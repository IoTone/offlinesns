//
// This example code uses the UART to talk to meshtastic
// and relays messages into this chat session running on 
// http://0.0.0.0:3333
//
// Use a chrome browser
//
// - build: cargo build
// - run: cargo run
//
//
// Attribution: https://github.com/Totodore/socketioxide/blob/main/examples/chat/src/main.rs
// and
// https://github.com/meshtastic/rust/blob/main/examples/basic_serial.rs
//
use std::sync::atomic::AtomicUsize;

use serde::{Deserialize, Serialize};
use socketioxide::{
    extract::{Data, Extension, SocketRef, State},
    SocketIo,
};
use tower::ServiceBuilder;
use tower_http::{cors::CorsLayer, services::ServeDir};
use tracing::info;
use tracing_subscriber::FmtSubscriber;
use std::sync::Arc;

extern crate meshtastic;

use std::io::{self, BufRead};
use std::time::SystemTime;

use meshtastic::api::StreamApi;
use meshtastic::utils;

#[derive(Deserialize, Serialize, Debug, Clone)]
#[serde(transparent)]
struct Username(String);

#[derive(Deserialize, Serialize, Debug, Clone)]
#[serde(rename_all = "camelCase", untagged)]
enum Res {
    Login {
        #[serde(rename = "numUsers")]
        num_users: usize,
    },
    UserEvent {
        #[serde(rename = "numUsers")]
        num_users: usize,
        username: Username,
    },
    Message {
        username: Username,
        message: String,
    },
    Username {
        username: Username,
    },
}
#[derive(Clone)]
struct UserCnt(Arc<AtomicUsize>);
impl UserCnt {
    fn new() -> Self {
        Self(Arc::new(AtomicUsize::new(0)))
    }
    fn add_user(&self) -> usize {
        self.0.fetch_add(1, std::sync::atomic::Ordering::SeqCst) + 1
    }
    fn remove_user(&self) -> usize {
        self.0.fetch_sub(1, std::sync::atomic::Ordering::SeqCst) - 1
    }
}


/// Set up the logger to output to stdout  
/// **Note:** the invocation of this function is commented out in main by default.
#[allow(dead_code)]
fn setup_logger() -> Result<(), fern::InitError> {
    fern::Dispatch::new()
        .format(|out, message, record| {
            out.finish(format_args!(
                "[{} {} {}] {}",
                humantime::format_rfc3339_seconds(SystemTime::now()),
                record.level(),
                record.target(),
                message
            ))
        })
        .level(log::LevelFilter::Trace)
        .chain(std::io::stdout())
        .apply()?;

    Ok(())
}

async fn run_meshtastic(io: SocketIo) -> anyhow::Result<()> {
    // Uncomment this to enable logging
    // setup_logger()?;

    let stream_api = StreamApi::new();

    let available_ports = utils::stream::available_serial_ports()?;
    println!("Available ports: {:?}", available_ports);
    println!("Enter the name of a port to connect to:");

    let stdin = io::stdin();
    let entered_port = stdin
        .lock()
        .lines()
        .next()
        .expect("Failed to find next line")
        .expect("Could not read next line");

    let serial_stream = utils::stream::build_serial_stream(entered_port, None, None, None)?;
    let (mut decoded_listener, stream_api) = stream_api.connect(serial_stream).await;

    let config_id = utils::generate_rand_id();
    let stream_api = stream_api.configure(config_id).await?;

    // This loop can be broken with ctrl+c, or by disconnecting
    // the attached serial port.
    while let Some(decoded) = decoded_listener.recv().await {
        println!("Received: {:?}", decoded);
        match io.emit("meshtastic-packet", &decoded) {
            Ok(()) => {
                // Emitted successfully
                println!("›› sent packet to clients: {:?}", decoded);
            }
            Err(err) => {
                // Only if something goes wrong with the socket emit:
                eprintln!("⚠️ emit error: {:?}", err);
            }
        }
    }

    // Note that in this specific example, this will only be called when
    // the radio is disconnected, as the above loop will never exit.
    // Typically you would allow the user to manually kill the loop,
    // for example with tokio::select!.
    let _stream_api = stream_api.disconnect().await?;

    Ok(())
}

async fn run_ws() -> anyhow::Result<()> {
    // Set up pub/sub for websocket
    let subscriber = FmtSubscriber::new();

    tracing::subscriber::set_global_default(subscriber)?;

    info!("Starting server libOFFSNS");

    let (layer,io) = SocketIo::builder().with_state(UserCnt::new()).build_layer();

    io.ns("/", |s: SocketRef| {
        s.on(
            "new message",
            |s: SocketRef, Data::<String>(msg), Extension::<Username>(username)| {
                let msg = Res::Message {
                    username,
                    message: msg,
                };
                s.broadcast().emit("new message", msg).ok();
            },
        );

        s.on(
            "add user",
            |s: SocketRef, Data::<String>(username), user_cnt: State<UserCnt>| {
                if s.extensions.get::<Username>().is_some() {
                    return;
                }
                let num_users = user_cnt.add_user();
                s.extensions.insert(Username(username.clone()));
                s.emit("login", Res::Login { num_users }).ok();

                let res = Res::UserEvent {
                    num_users,
                    username: Username(username),
                };
                s.broadcast().emit("user joined", res).ok();
            },
        );

        s.on("typing", |s: SocketRef, Extension::<Username>(username)| {
            s.broadcast()
                .emit("typing", Res::Username { username })
                .ok();
        });

        s.on(
            "stop typing",
            |s: SocketRef, Extension::<Username>(username)| {
                s.broadcast()
                    .emit("stop typing", Res::Username { username })
                    .ok();
            },
        );

        s.on_disconnect(
            |s: SocketRef, user_cnt: State<UserCnt>, Extension::<Username>(username)| {
                let num_users = user_cnt.remove_user();
                let res = Res::UserEvent {
                    num_users, 
                    username,
                };
                s.broadcast().emit("user left", res).ok();
            },
        );
    });

    let app = axum::Router::new()
        .nest_service("/", ServeDir::new("dist"))
        .layer(
            ServiceBuilder::new()
                .layer(CorsLayer::permissive()) // Enable CORS policy
                .layer(layer),
        );

    let listener = tokio::net::TcpListener::bind("0.0.0.0:3333").await.unwrap();
    axum::serve(listener, app).await.unwrap();

    
    Ok(())
}
#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
   
    //
    // Alternative
    /*
    // Spawn each future on the Tokio runtime
    let mesh_task  = tokio::spawn(async { run_meshtastic().await });
    let server_task = tokio::spawn(async { run_webserver().await });

    // Wait for either to exit (or error)
    tokio::select! {
        res = mesh_task  => {
            eprintln!("Meshtastic task ended: {:?}", res);
        },
        res = server_task => {
            eprintln!("Webserver task ended: {:?}", res);
        },
    }
     */
    // Spawn the two independent futures…
    let mesh_fut   = run_meshtastic();
    let server_fut = run_ws();

    // …then drive them together until *both* complete (or error).
    let (mesh_res, server_res) = tokio::join!(mesh_fut, server_fut);

    // Propagate errors if either returned Err:
    mesh_res?;      // run_meshtastic()
    server_res?;    // run_webserver()
    

    
    Ok(())
}