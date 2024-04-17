let stompClient = null;
let inventory = {
  product1: 0,
  product2: 0,
};

function setConnected(connected) {
  document.getElementById("connect").disabled = connected;
  document.getElementById("disconnect").disabled = !connected;
  if (connected) {
    document.getElementById("messages").style.display = "block";
    document.getElementById("messages").innerHTML = "";
  }
}

function connect() {
  console.log(document.getElementById("websocketURL").value);
  const socket = new SockJS(document.getElementById("websocketURL").value);
  stompClient = Stomp.over(socket);
  const headers = {};
  if (document.getElementById("websocketToken").value !== "") {
    headers["Authorization"] =
      "Bearer " + document.getElementById("websocketToken").value;
  }
  console.log(headers);
  stompClient.connect(
    headers,
    function (frame) {
      setConnected(true);
      console.log("Connected: " + frame);
      const headers = {};
      if (document.getElementById("websocketToken").value !== "") {
        headers["Authorization"] =
          "Bearer " + document.getElementById("websocketToken").value;
      }
      stompClient.subscribe(
        document.getElementById("websocketTopic").value,
        function (message) {
          console.log(message);
          const data = JSON.parse(message.body).data;
          console.log(data);
          updateProducts(data.id, data);
          showMessage("[ in ] " + message.body);
          handleInventoryUpdate(JSON.parse(message.body));
        },
        headers
      );
    },
    (err) => {
      console.log(err);
      showMessage("[ err ] " + err);
      setConnected(false);
    }
  );
}

function disconnect() {
  if (stompClient !== null) {
    stompClient.disconnect();
  }
  setConnected(false);
  console.log("Disconnected");
}

function sendMessage() {
  const headers = {};
  if (document.getElementById("websocketToken").value !== "") {
    headers["Authorization"] =
      "Bearer " + document.getElementById("websocketToken").value;
  }
  stompClient.send(
    document.getElementById("websocketTopic").value,
    headers,
    JSON.stringify(document.getElementById("message").value)
  );
  showMessage("[ out ] " + document.getElementById("message").value);
}

function showMessage(message) {
  const table = document.getElementById("messages");
  const row = table.insertRow(-1);
  const cell = row.insertCell(0);
  cell.appendChild(document.createTextNode(message));
}

function handleInventoryUpdate(update) {
  if (update && update.productId && update.inventoryCount !== undefined) {
    inventory["product" + update.productId] = update.inventoryCount;
    updateInventoryDisplay();
  }
}

function updateInventoryDisplay() {
  for (const productId in inventory) {
    if (Object.hasOwnProperty.call(inventory, productId)) {
      const countElement = document.getElementById(productId + "Count");
      if (countElement) {
        countElement.textContent = inventory[productId];
      }
    }
  }
}

window.onload = function () {
  document.getElementById("connect").addEventListener("click", function (e) {
    e.preventDefault();
    connect();
  });
  document.getElementById("disconnect").addEventListener("click", function (e) {
    e.preventDefault();
    disconnect();
  });
  document.getElementById("send").addEventListener("click", function (e) {
    e.preventDefault();
    sendMessage();
  });

  document.querySelector("form").addEventListener("submit", function (e) {
    e.preventDefault();
  });

  fetchProducts();
  fetchAndRenderImages();
  // fetchSingleImage();
};

function fetchProducts() {
  const token = document.getElementById("websocketToken").value;
  const headers = {};
  if (token !== "") {
    headers["Authorization"] = "Bearer " + token;
  }

  fetch("http://localhost:8080/product", {
    headers: headers,
  })
    .then((response) => response.json())
    .then((products) => {
      // Assuming products is an array of product objects
      products.forEach((product) => {
        // Display each product in your HTML
        const productElement = document.createElement("div");
        productElement.textContent = `Product ID: ${product.id}, Name: ${product.name}, Price: ${product.price}`;
        document
          .getElementById("productsContainer")
          .appendChild(productElement);
      });
    })
    .catch((error) => {
      console.error("Error fetching products:", error);
    });
}

function updateProducts(id, product) {
  // Assuming productsContainer is the ID of the container element holding the product divs
  const productsContainer = document.getElementById("productsContainer");

  // Loop through each div element inside the container
  productsContainer.querySelectorAll("div").forEach((div) => {
    if (id == extractProductId(div.textContent)) {
      div.textContent = `Product ID: ${product.id}, Name: ${product.name}, Price: ${product.price}`;
    }
  });
}

// Function to extract the product ID from the text content
function extractProductId(text) {
  const idStartIndex = text.indexOf("Product ID:") + "Product ID:".length; // Find the index of "Product ID:" and add its length
  const idEndIndex = text.indexOf(","); // Find the index of the comma after the id
  const id = text.substring(idStartIndex, idEndIndex).trim(); // Extract the id substring and remove leading/trailing spaces
  return id;
}

function fetchSingleImage() {
  fetch("http://localhost:8080/image/2/gallery")
    .then((response) => response.blob()) // Retrieve response as Blob
    .then((blob) => {
      const imageUrl = URL.createObjectURL(blob); // Create a URL for the Blob
      const imageContainer = document.getElementById("imageContainer");
      const img = document.createElement("img");
      img.src = imageUrl;
      img.style.width = "200px"; // Adjust dimensions as needed
      img.style.height = "auto"; // Maintain aspect ratio
      imageContainer.appendChild(img);
    })
    .catch((error) => {
      console.error("Error fetching image:", error);
    });
}

async function fetchAndRenderImages() {
  try {
    const response = await fetch(
      "http://localhost:8080/image/35147d6a-96d3-4e08-9ad7-78495b970896/gallery"
    );
    const zipFileBlob = await response.blob();

    const blobArrayBuffer = await zipFileBlob.arrayBuffer();
    const newBlob = new Blob([blobArrayBuffer]);

    const zip = await JSZip.loadAsync(newBlob);
    console.log(zip);

    const imageContainer = document.getElementById("imageContainer");

    await Promise.all(
      Object.keys(zip.files).map(async (filename) => {
        const file = zip.files[filename];

        if (filename.match(/\.(jpg|jpeg|png|gif)$/i)) {
          const imageData = await file.async("blob");
          const imageUrl = URL.createObjectURL(imageData);
          const img = document.createElement("img");
          img.src = imageUrl;
          img.title = filename;
          imageContainer.appendChild(img);
        }
      })
    );
  } catch (error) {
    console.error("Error fetching or rendering images:", error);
  }
}

// Call the function to fetch and render images when the page loads
// window.onload = fetchAndRenderImages;
