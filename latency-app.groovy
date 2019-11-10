/**
 *  Hubitat Import URL: https://github.com/as-j/LatencySensor/raw/master/latency-app.groovy
 *
 *  Latency App, uses TimedSession to schedule and time events
 *
 *  Copyright 2019 A Stanley-Jones
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
public static String version()      {  return "v1.0"  }

definition(
    name: "Latency Test App",
    namespace: "asj",
    author: "asj",
    description: "Record latency from On->Off via App",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {

    preferences {
        page(name: "mainPage", title: "Settings Page", install: true, uninstall: true) {
            section() {
                label title: "App Name", defaultValue: app.label, required: true
                input "scheduleString", "text", title: "Host", defaultValue: "0 6,16,26,36,46,56 * * * ?", required: true
                input "timedSession", "capability.timedSession", title: "Timed Session", multiple: true, required: false
            }
            section() {
                //standard logging options
                input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
                input name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: true
            }
        }

    }
}


/**
 *  installed()
 *
 *  Runs when the app is first installed.
 **/
def installed() {
    state.installedAt = now()
    state.loggingLevelIDE = 5
    log.debug "${app.label}: Installed with settings: ${settings}" 
    updated()
}

/**
 *  uninstalled()
 *
 *  Runs when the app is uninstalled.
 **/
def uninstalled() {
    if (logEnable) log.debug "${app.label}: Uninstalled"
}

/**
 *  updated()
 * 
 *  Runs when app settings are changed.
 * 
 *  Updates device.state with input values and other hard-coded values.
 *  Builds state.deviceAttributes which describes the attributes that will be monitored for each device collection 
 *  Refreshes scheduling and subscriptions.
 **/
def updated() {
    if (logEnable) log.debug "${app.label}: updated ${settings.timedSession} / ${settings.scheduleString}"

    unsubscribe()
    unschedule()

    subscribe(settings.timedSession, "sessionStatus.running", sessionRunning)
    schedule(settings.scheduleString, "trigger")

    settings.timedSession.start()

}

/**
 *  sessionChange(evt)
 *
 **/
def sessionRunning(evt) {
    settings.timedSession.stop()
    if (logEnable) log.debug "handleEvent(): $evt.displayName($evt.name) $evt.value"
}

def trigger() {
    settings.timedSession.start()
    runIn(5, forceStop)
}

def forceStop() {
    settings.timedSession.stop()
}


