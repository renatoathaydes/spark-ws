var host = window.location.hostname;

var wsUri = "ws://" + host + ":8025/chat";
var input, output, websocket, sendButton;

function init() {
  input = document.getElementById("input");
  output = document.getElementById("output");
  sendButton = document.getElementById("send-button");
  disconnect();
  input.onkeyup = inputKeyup;
}

function inputKeyup(event) {
  if (event.keyCode == 13) {
    sendText();
  }
}

function initWebsocket() {
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
  if (!websocket) {
    initWebsocket();
  }
  sendButton.disabled = false;
}

function disconnect() {
  if (websocket) {
    websocket.close();
  }
  websocket = false;
  sendButton.disabled = true;
}

function onError(evt) {
  writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

function sendText() {
  if (websocket) {
    websocket.send(input.value);
    input.value = "";
  }
}

function writeToScreen(message) {
  var pre = document.createElement("p");
  pre.style.wordWrap = "break-word";
  pre.innerHTML = message;
  output.appendChild(pre);
  try {
    output.scrollTop = output.scrollHeight;
  } catch (e) {}
}

window.addEventListener("load", init, false);
