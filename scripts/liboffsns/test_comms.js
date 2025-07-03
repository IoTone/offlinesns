const { interfaceFactory, commandsFactory } = require("meshtastic-js");

// TODO: make this discover the uarts
const interface = interfaceFactory("/dev/cu.usbserial-0001");
const commands = commandsFactory(interface);

commands.getNodeDB().then(data => {
	console.log(data);
});
