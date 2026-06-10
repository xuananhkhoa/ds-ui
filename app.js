const chat = document.getElementById("chat");
const chatForm = document.getElementById("chatForm");
const messageInput = document.getElementById("messageInput");
const sendButton = document.getElementById("sendButton");
const hero = document.getElementById("hero");

const COORDINATOR_BASE_URL = "";
const POLL_INTERVAL_MS = 1500;
const MAX_POLL_ATTEMPTS = 30;
const PROCESSING_STATES = new Set(["received", "formatted", "unformatted result"]);

function addMessage(role, text, results = []) {
    const message = document.createElement("div");
    message.classList.add("message");

    if (role === "user") {
        message.classList.add("user-message");
    } else {
        message.classList.add("assistant-message");
    }

    const avatar = document.createElement("div");
    avatar.classList.add("avatar");
    avatar.textContent = role === "user" ? "U" : "AI";

    const bubble = document.createElement("div");
    bubble.classList.add("bubble");
    bubble.textContent = text;

    results.forEach(result => {
        bubble.appendChild(createResultCard(result));
    });

    message.appendChild(avatar);
    message.appendChild(bubble);

    chat.appendChild(message);
    scrollToBottom();
}

function createResultCard(result) {
    const card = document.createElement("div");
    card.classList.add("result-card");

    const dishName = result.dish || result.dishName || result.name || "Unknown Dish";
    const matchScore = result.matchScore || 0;
    const missingIngredients = result.missingIngredients || [];

    const title = document.createElement("div");
    title.classList.add("result-title");
    title.textContent = dishName;

    const missing = document.createElement("div");
    missing.classList.add("result-info");

    const missingText = missingIngredients.length > 0
        ? missingIngredients.join(", ")
        : "None";

    missing.innerHTML = `<strong>Missing ingredients:</strong> ${missingText}`;

    const match = document.createElement("div");
    match.classList.add("match-pill");
    match.textContent = `Match ${Math.round(matchScore * 100)}%`;

    card.appendChild(title);
    card.appendChild(missing);
    card.appendChild(match);

    return card;
}

function addTypingMessage() {
    const message = document.createElement("div");
    message.classList.add("message", "assistant-message");
    message.id = "typingMessage";

    const avatar = document.createElement("div");
    avatar.classList.add("avatar");
    avatar.textContent = "AI";

    const bubble = document.createElement("div");
    bubble.classList.add("bubble", "typing");
    bubble.textContent = "Thinking...";

    message.appendChild(avatar);
    message.appendChild(bubble);

    chat.appendChild(message);
    scrollToBottom();
}

function removeTypingMessage() {
    const typingMessage = document.getElementById("typingMessage");

    if (typingMessage) {
        typingMessage.remove();
    }
}

function scrollToBottom() {
    window.scrollTo({
        top: document.body.scrollHeight,
        behavior: "smooth"
    });
}

function hideHeroAfterFirstMessage() {
    if (hero) {
        hero.style.display = "none";
    }
}

async function callCoordinatorSearch(userMessage) {
    const response = await fetch(`${COORDINATOR_BASE_URL}/search`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            userQuery: userMessage
        })
    });

    if (!response.ok) {
        throw new Error(`Coordinator returned ${response.status}`);
    }

    const requestId = (await response.text()).trim();
    const result = await pollCoordinatorResult(requestId);

    return {
        reply: result,
        results: []
    };
}

async function pollCoordinatorResult(requestId) {
    for (let attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt += 1) {
        const response = await fetch(
            `${COORDINATOR_BASE_URL}/get?id=${encodeURIComponent(requestId)}`
        );

        if (!response.ok) {
            throw new Error(`Coordinator returned ${response.status}`);
        }

        const text = (await response.text()).trim();

        if (!PROCESSING_STATES.has(text)) {
            return text;
        }

        await new Promise(resolve => setTimeout(resolve, POLL_INTERVAL_MS));
    }

    return "The request is still processing. Try again in a moment.";
}

async function getAssistantResponse(userMessage) {
    return await callCoordinatorSearch(userMessage);
}

async function sendMessage(messageText) {
    hideHeroAfterFirstMessage();

    addMessage("user", messageText);
    addTypingMessage();

    sendButton.disabled = true;

    try {
        const data = await getAssistantResponse(messageText);

        removeTypingMessage();

        addMessage(
            "assistant",
            data.reply || "I found some results.",
            data.results || []
        );

    } catch (error) {
        removeTypingMessage();

        addMessage(
            "assistant",
            "I could not connect to the Coordinator. Make sure the Coordinator is running on port 8080."
        );

        console.error(error);

    } finally {
        sendButton.disabled = false;
    }
}

chatForm.addEventListener("submit", function (event) {
    event.preventDefault();

    const messageText = messageInput.value.trim();

    if (!messageText) {
        return;
    }

    messageInput.value = "";
    messageInput.style.height = "auto";

    sendMessage(messageText);
});

messageInput.addEventListener("input", function () {
    messageInput.style.height = "auto";
    messageInput.style.height = messageInput.scrollHeight + "px";
});

messageInput.addEventListener("keydown", function (event) {
    if (event.key === "Enter" && !event.shiftKey) {
        event.preventDefault();
        chatForm.requestSubmit();
    }
});
