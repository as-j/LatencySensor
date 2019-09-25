/*
   System Delay Sensor
 */


metadata {
    definition (name: "Latency Sensor", namespace: "asj", author: "asj") {
        capability "Switch"
        capability "PressureMeasurement"
        capability "Sensor"
    }

    preferences {
        input name: "autoOff", type: "bool", title: "Auto Off Timing", defaultValue: true
        //standard logging options
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false
        input name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: false
    }
}

def logsOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

//capability and device methods
def off() {
   state.endTime = new Date().getTime()
   state.latency = state.endTime - state.startTime
   if (txtEnable) log.info "${device.displayName} ${name} On to off time took: ${state.latency}"
   def name = "pressure"
   def unit = "kPa"
   def descriptionText = "${device.displayName} ${name} is ${value}${unit}"
   sendEvent(name: "pressure",value: state.latency, descriptionText: descriptionText,unit: unit)
   sendEvent(name: "switch", value: "off", isStateChange: true)
}

def on() {
    sendEvent(name: "switch", value: "on", isStateChange: true)
    state.startTime = new Date().getTime()
    if (autoOff) runInMillis(50, off)
}

