function ensureCustomer() {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");
  const username = localStorage.getItem("username");

  if (!token || role !== "USER") {
    window.location.replace("/login.html");
    return false;
  }

  const welcome = document.getElementById("welcomeText");
  if (welcome) {
    welcome.innerText = `Welcome, ${username}`;
  }

  return true;
}

function getToken() {
  return localStorage.getItem("token");
}

function goToCart() {
  window.location.href = "/view-cart.html";
}

function goBackToShopping() {
  window.location.href = "/index.html";
}

// --- ADD TO CART FUNCTION WITH LOGIN CHECK ---
function addToCart(productId) {
  const token = localStorage.getItem("token");

  if (!token) {
    alert("Please log in first to purchase items.");
    window.location.href = "/login.html";
    return;
  }

  fetch(`/api/cart/add?productId=${productId}`, {
    method: "POST",
    headers: {
      "Authorization": "Bearer " + token,
      "Content-Type": "application/json"
    }
  })
  .then(async response => {
    if (response.ok) {
      alert("Product added to cart successfully!");
    } else {
      const errText = await response.text();
      alert("Failed to add product to cart: " + errText);
    }
  })
  .catch(error => {
    console.error("Error adding to cart:", error);
    alert("An error occurred while adding to cart.");
  });
}

// --- NEW: RENDER CATEGORIES 5 PER ROW SIDE-BY-SIDE ---
async function loadCategories() {
  const container = document.getElementById("category-slideshow-container");
  if (!container) return;

  try {
    const response = await fetch("/api/products", {
      headers: { "Authorization": "Bearer " + getToken() }
    });

    if (!response.ok) return;

    const products = await response.json();
    
    // Group first product of each category to get clean thumbnails
    const categoryMap = {};
    products.forEach(p => {
      const cat = p.category || "OTHER";
      if (!categoryMap[cat]) {
        categoryMap[cat] = p;
      }
    });

    let html = "";
    Object.keys(categoryMap).forEach(catName => {
      const p = categoryMap[catName];
      html += `
        <div style="flex: 0 0 calc(20% - 16px); width: calc(20% - 16px); box-sizing: border-box; background: #faf5ff; border: 1px solid #f3e8ff; border-radius: 12px; padding: 10px; text-align: center; cursor: pointer;" onclick="filterCategory('${catName}')">
          <img src="${p.imageUrl || ''}" alt="${catName}" style="width: 100%; height: 110px; object-fit: cover; border-radius: 8px; margin-bottom: 8px;" />
          <h4 style="margin: 0; color: #7e22ce; font-size: 14px; font-weight: 600;">${catName}</h4>
          <p style="margin: 4px 0 0 0; color: #a855f7; font-size: 11px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
            ${p.name}
          </p>
        </div>
      `;
    });

    container.style.display = "flex";
    container.style.flexWrap = "wrap";
    container.style.justifyContent = "flex-start";
    container.style.gap = "20px";
    container.innerHTML = html;

  } catch (err) {
    console.error("Error loading category cards:", err);
  }
}

// --- LOAD CART WITH IMAGES, UPDATE BUTTON, AND GRAND TOTAL ---
async function loadCart() {
  const cartList = document.getElementById("cartList");
  if (!cartList) return;

  const response = await fetch("/api/cart", {
    headers: {
      "Authorization": "Bearer " + getToken()
    }
  });

  if (response.status === 401) {
    logout();
    return;
  }

  if (response.status === 403) {
    alert("Access denied to cart");
    return;
  }

  const data = await response.json();

  let cartItems = [];
  if (Array.isArray(data)) {
    cartItems = data;
  } else if (data && Array.isArray(data.items)) {
    cartItems = data.items;
  } else if (data && Array.isArray(data.cartItems)) {
    cartItems = data.cartItems;
  }

  if (!cartItems || cartItems.length === 0) {
    cartList.innerHTML = `
      <div style="text-align: center; padding: 40px; background: white; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
        <p style="font-size: 18px; color: #64748b; margin-bottom: 20px;">Nothing is in the cart.</p>
        <button onclick="goBackToShopping()" style="padding: 10px 20px; background: #2563eb; color: white; border: none; border-radius: 6px; cursor: pointer;">
          Start Shopping
        </button>
      </div>
    `;
    return;
  }

  let html = `
    <table class="product-table" style="width: 100%; border-collapse: collapse; background: white; box-shadow: 0 2px 5px rgba(0,0,0,0.1); margin-bottom: 20px;">
      <tr style="background: #f1f5f9; text-align: left;">
        <th style="padding: 10px;">Image</th>
        <th style="padding: 10px;">Product</th>
        <th style="padding: 10px;">Price</th>
        <th style="padding: 10px;">Quantity</th>
        <th style="padding: 10px;">Total</th>
        <th style="padding: 10px;">Action</th>
      </tr>
  `;

  let grandTotal = 0;

  cartItems.forEach(item => {
    const cartItemId = item.cartItemId !== undefined ? item.cartItemId : item.id;
    const itemTotal = item.totalPrice !== undefined ? item.totalPrice : (item.price * item.quantity);
    grandTotal += itemTotal;

    const maxStock = item.stock !== undefined ? item.stock : (item.productStock !== undefined ? item.productStock : 99);
    const imageUrl = item.imageUrl || item.productImage || '';

    html += `
      <tr style="border-bottom: 1px solid #eee;">
        <td style="padding: 10px;">
          <img src="${imageUrl}" alt="Product" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;" />
        </td>
        <td style="padding: 10px;">${item.productName || item.name || 'Product'}</td>
        <td style="padding: 10px;">₹${item.price}</td>
        <td style="padding: 10px;">
          <input type="number" id="qty-${cartItemId}" value="${item.quantity}" min="1" style="width: 60px; padding: 4px;" />
          <button onclick="updateCartItem(${cartItemId}, ${maxStock})" style="padding: 4px 8px; background: #10b981; color: white; border: none; border-radius: 4px; cursor: pointer; margin-left: 5px;">Update</button>
        </td>
        <td style="padding: 10px;">₹${itemTotal}</td>
        <td style="padding: 10px;">
          <button onclick="removeCartItem(${cartItemId})" style="padding: 6px 12px; background: #ef4444; color: white; border: none; border-radius: 4px; cursor: pointer;">Remove</button>
        </td>
      </tr>
    `;
  });

  html += `</table>`;
  
  html += `
    <div style="text-align: right; background: white; padding: 15px 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); margin-bottom: 20px;">
      <h2 style="margin: 0; color: #1e293b;">Grand Total: ₹${grandTotal}</h2>
    </div>
  `;

  cartList.innerHTML = html;
}

// --- UPDATE CART ITEM WITH STOCK EXCEED MESSAGES ---
async function updateCartItem(cartItemId, maxStock) {
  const input = document.getElementById(`qty-${cartItemId}`);
  if (!input) return;

  let qty = parseInt(input.value);

  if (qty > maxStock) {
    alert(`You selected exceeding the stock limit! Only ${maxStock} items are available in stock.`);
    input.value = maxStock;
    return;
  } else if (qty === maxStock) {
    alert(`You have selected the entire stock on sale (${maxStock} items).`);
  } else if (qty < 1) {
    alert("Quantity must be at least 1.");
    input.value = 1;
    return;
  }

  const response = await fetch(`/api/cart/${cartItemId}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      "Authorization": "Bearer " + getToken()
    },
    body: JSON.stringify({
      quantity: Number(qty)
    })
  });

  const result = await response.json().catch(() => ({}));

  if (!response.ok) {
    alert(result.message || "Failed to update cart item");
    return;
  }

  alert(result.message || "Cart updated successfully!");
  loadCart();
}

// --- REMOVE CART ITEM ---
async function removeCartItem(cartItemId) {
  const response = await fetch(`/api/cart/${cartItemId}`, {
    method: "DELETE",
    headers: {
      "Authorization": "Bearer " + getToken()
    }
  });

  const result = await response.json().catch(() => ({}));

  if (!response.ok) {
    alert(result.message || "Failed to remove cart item");
    return;
  }

  alert("Item removed from cart");
  loadCart();
}

// checkout handler
async function checkout() {
  try {
    const checkResponse = await fetch("/api/cart/is-empty", {
      headers: {
        "Authorization": "Bearer " + localStorage.getItem("token")
      }
    });

    if (checkResponse.ok) {
      const isEmpty = await checkResponse.json();
      if (isEmpty) {
        alert("Your cart is empty! Please add items before proceeding to buy.");
        return;
      }
    }

    const response = await fetch("/customer/payment/create-order", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + localStorage.getItem("token")
      }
    });

    if (response.status === 401 || response.status === 403) {
      alert("Session expired or access denied. Please log in again.");
      logout();
      return;
    }

    if (!response.ok) {
      alert("Could not create payment order. Please try again.");
      return;
    }

    const order = await response.json();
    console.log("Order created:", order);

    const options = {
      key: order.keyId,
      amount: order.amount,
      currency: order.currency || "INR",
      name: "Anu Jewellery",
      description: "Purchase from Cart",
      order_id: order.razorpayOrderId,
      handler: async function (response) {
        try {
          const verifyResponse = await fetch("/customer/payment/verify", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              "Authorization": "Bearer " + localStorage.getItem("token")
            },
            body: JSON.stringify({
              razorpayPaymentId: response.razorpay_payment_id,
              razorpayOrderId: response.razorpay_order_id,
              razorpaySignature: response.razorpay_signature
            })
          });

          if (verifyResponse.ok) {
            alert("Payment successful! Your order has been placed.");
            window.location.href = "index.html";
          } else {
            alert("Payment verification failed.");
          }
        } catch (err) {
          console.error("Verification error:", err);
          alert("An error occurred during payment verification.");
        }
      },
      prefill: {
        name: localStorage.getItem("username") || ""
      },
      theme: {
        color: "#7e22ce"
      }
    };

    const rzp = new Razorpay(options);
    rzp.open();

  } catch (error) {
    console.error("Checkout error:", error);
    alert("Could not initiate payment. Please try again.");
  }
}

// --- LOAD PRODUCTS FOR SHOPPING HOME ---
async function loadProducts() {
  const response = await fetch("/api/products", {
    headers: {
      "Authorization": "Bearer " + getToken()
    }
  });

  if (response.status === 401) {
    logout();
    return;
  }

  if (response.status === 403) {
    alert("Access denied to products");
    return;
  }

  const products = await response.json();
  const shoppingSection = document.getElementById("shoppingSection");
  const productList = document.getElementById("productList");

  if (shoppingSection) {
    shoppingSection.style.display = "block";
  }

  if (!productList) return;

  if (!products.length) {
    productList.innerHTML = "<p>No products found</p>";
    return;
  }

  const categories = {};
  products.forEach(product => {
    const cat = product.category || "OTHER";
    if (!categories[cat]) {
      categories[cat] = [];
    }
    categories[cat].push(product);
  });

  let html = "";

  for (const [categoryName, catProducts] of Object.entries(categories)) {
    html += `
      <div class="category-section" style="margin-bottom: 30px;">
        <h3 style="background: #2563eb; color: white; padding: 10px 15px; border-radius: 6px 6px 0 0; margin-bottom: 0;">
          ${categoryName}
        </h3>
        <table class="product-table" style="width: 100%; border-collapse: collapse; background: white; box-shadow: 0 2px 5px rgba(0,0,0,0.1); margin-bottom: 20px;">
          <tr style="background: #f1f5f9; text-align: left;">
            <th style="padding: 10px;">Image</th>
            <th style="padding: 10px;">Name</th>
            <th style="padding: 10px;">Price</th>
            <th style="padding: 10px;">Stocks</th>
            <th style="padding: 10px;">Action</th>
          </tr>
    `;

    catProducts.forEach(product => {
      html += `
        <tr style="border-bottom: 1px solid #eee;">
          <td style="padding: 10px;">
            <img src="${product.imageUrl || ''}" alt="${product.name}" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;" />
          </td>
          <td style="padding: 10px;">${product.name}</td>
          <td style="padding: 10px;">₹${product.price}</td>
          <td style="padding: 10px;">${product.stock}</td>
          <td style="padding: 10px;">
            <button onclick="addToCart(${product.id})" style="padding: 6px 12px; background: #2563eb; color: white; border: none; border-radius: 4px; cursor: pointer;">Add to Cart</button>
          </td>
        </tr>
      `;
    });

    html += `
        </table>
      </div>
    `;
  }

  productList.innerHTML = html;
}

function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("username");
  localStorage.removeItem("role");
  window.location.replace("/index.html");
}

document.addEventListener("DOMContentLoaded", () => {
  if (!ensureCustomer()) return;

  if (document.getElementById("category-slideshow-container")) {
    loadCategories();
  }
  if (document.getElementById("productList")) {
    loadProducts();
  }
  if (document.getElementById("cartList")) {
    loadCart();
  }
});