
var wsUri = "ws://localhost:8025/chat";
var output, websocket;

function init() {
  if (websocket !== undefined) {
    disconnect();
  }
  output = document.getElementById("output");
}

function testWebSocket() {
  websocket = new WebSocket(wsUri);
  websocket.onopen = function(evt) { onOpen(evt) };
  websocket.onclose = function(evt) { onClose(evt) };
  websocket.onmessage = function(evt) { onMessage(evt) };
  websocket.onerror = function(evt) { onError(evt) };
}

function onOpen(evt) {
  writeToScreen("CONNECTED");
}

function onClose(evt) {
  writeToScreen("DISCONNECTED");
}

function onMessage(evt) {
  writeToScreen('<span style="color: blue;">' + evt.data + '</span>');
}

function connect() {
  if (websocket === undefined) {
    testWebSocket();
  }
}

function disconnect() {
  if (websocket !== undefined) {
    websocket.close();
    websocket = undefined;
  }
}

function onError(evt) {
  writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

function sendText() {
  var message = document.getElementById("input").value;
  websocket.send(message);
}

function writeToScreen(message) {
  var pre = document.createElement("p");
  pre.style.wordWrap = "break-word";
  pre.innerHTML = message;
  output.appendChild(pre);
}

window.addEventListener("load", init, false);
