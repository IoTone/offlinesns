# libmeshcore-android consumer ProGuard rules.
# The protocol codec and model classes must be kept if obfuscation is
# applied to a consuming app (frame field names are referenced by name
# in fixture JSON and in log output).
-keep class io.iotone.meshcore.** { *; }
