#! /bin/sh

# <codex>
# <abstract>Script to remove everything installed by the sample.</abstract>
# </codex>

# This uninstalls everything installed by the sample.  It's useful when testing to ensure that 
# you start from scratch.

sudo launchctl unload /Library/LaunchDaemons/com.davidrueter.shenanigans.PcapHelperTool.plist
sudo rm /Library/LaunchDaemons/com.davidrueter.shenanigans.PcapHelperTool.plist
sudo rm /Library/PrivilegedHelperTools/com.davidrueter.shenanigans.PcapHelperTool

sudo security -q authorizationdb remove "com.example.apple-samplecode.EBAS.readLicenseKey"
sudo security -q authorizationdb remove "com.example.apple-samplecode.EBAS.writeLicenseKey"
sudo security -q authorizationdb remove "com.example.apple-samplecode.EBAS.startWebService"

sudo security -q authorizationdb remove "drueter.shenanigans.PcapHelperTool.chmodBPF"

sudo defaults delete com.davidrueter.shenanigans.PcapHelperTool licenseKey
