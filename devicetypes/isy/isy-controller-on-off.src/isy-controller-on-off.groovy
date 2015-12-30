/**
 *  ISY Controller: On-Off
 *
 *  Copyright 2015 Kyle Landreth
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
metadata {
	definition (name: "ISY Controller: On-Off", namespace: "isy", author: "Kyle Landreth") {
		capability "Actuator"
        capability "Sensor"
        capability "Switch"
        //capability "Polling"
        capability "Refresh"
	}

	simulator {
		// status messages
        //status "on": "on/off: 1"
        //status "off": "on/off: 0"
        
        // reply messages
        //reply "zcl on-off on": "on/off: 1"
        //reply "zcl on-off off": "on/off: 0"
	}

	// UI tile definitions
	tiles(scale: 1){
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true){
        	state "off", label: "off", action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "on", label: "on", action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main "switch"
        details (["switch", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
    
    /* parse this for each device
    	<properties>
   			<property id="ST" value="0" formatted="Off" uom="%/on/off"/>
		</properties>
    */

    /*part = part.trim()
        if (part.startsWith('devicetype:')) {
            def valueString = part.split(":")[1].trim()
            device.devicetype = valueString
      }*/
/*       
    def msg = parseLanMessage(description)

    def headerMap = msg.headers      // => headers as a Map   
       
    def attrName = null
    def attrValue = null

    if (description?.startsWith("value=")) {
        log.debug "switch value"
        attrName = "switch"
        attrValue = description?.endsWith("1") ? "on" : "off"
    }

    def result = createEvent(name: attrName, value: attrValue)

    log.debug "Parse returned ${result?.descriptionText}"
    return result
*/    
}

// switch on handler (from Richard L. Lynch <rich@richlynch.com>)
def on() {
    log.debug "Executing 'on'"

    sendEvent(name: 'switch', value: 'on')
    def node = getDataValue("nodeAddr").replaceAll(" ", "%20")
    def path = "/rest/nodes/${node}/cmd/DON"
    getRequest(path)
}

// switch off handler (from Richard L. Lynch <rich@richlynch.com>)
def off() {
    log.debug "Executing 'off'"

    sendEvent(name: 'switch', value: 'off')
    def node = getDataValue("nodeAddr").replaceAll(" ", "%20")
    def path = "/rest/nodes/${node}/cmd/DOF"
    getRequest(path)
}

// polling handler (from Richard L. Lynch <rich@richlynch.com>)
/*def poll() {
    if (!device.deviceNetworkId.contains(':')) {
        log.debug "Executing 'poll' from ${device.deviceNetworkId}"

        def path = "/rest/status"
        getRequest(path)
    }
    else {
        log.debug "Ignoring poll request for ${device.deviceNetworkId}"
    }
}*/

// refresh handler (from Richard L. Lynch <rich@richlynch.com>)
def refresh() {
    log.debug "Executing 'refresh'"

    def path = "/rest/status"
    getRequest(path)
}



// get request to the ISY for any of the get commands (from Richard L. Lynch <rich@richlynch.com>)
def getRequest(path) {
    log.debug "Sending request for ${path} from ${device.deviceNetworkId}"

    new physicalgraph.device.HubAction(
        'method': 'GET',
        'path': path,
        'headers': [
            'HOST': getHostAddress(),
            'Authorization': getAuthorization()
        ], device.deviceNetworkId)
}

// gets the address of the hub (from SmartThings Doc1)
private getCallBackAddress() {
    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

// gets the address of the device (from SmartThings Doc1)
private getHostAddress() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")

    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    }

    log.debug "Using IP: $ip and port: $port for device: ${device.id}"
    return convertHexToIP(ip) + ":" + convertHexToInt(port)
}

// provides the username and password to log into the ISY (from Richard L. Lynch <rich@richlynch.com>)
// format: <username>:<password> ??????
private getAuthorization() {
    def userpassascii = getDataValue("username") + ":" + getDataValue("password")
    "Basic " + userpassascii.encodeAsBase64().toString()    // ??????
}

// convert Hex to Integer (from SmartThings Doc1)
private Integer convertHexToInt(hex) {
    return Integer.parseInt(hex,16)
}

// convert Hex to IP (from SmartThings Doc1)
private String convertHexToIP(hex) {
    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

/** Documentation Used from SmartThings
* Doc1: http://docs.smartthings.com/en/latest/cloud-and-lan-connected-device-types-developers-guide/building-lan-connected-device-types/building-the-device-type.html#making-outbound-http-calls-with-hubaction
*
*
*/