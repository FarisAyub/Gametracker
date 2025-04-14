// Modal for viewing a game in the game list
const modalElement = document.getElementById('gameModal'); // Find the modal by id
const modal = new bootstrap.Modal(modalElement); // Create modal

// On clicking on the card, get the data attributes from the card and then assign them to the fields in the modal
modalElement.addEventListener('show.bs.modal', function (event) {
    const card = event.relatedTarget // Find the card that triggered the modal
    const gameId = card.getAttribute('data-bs-game-id'); // Get the games ID
    const gameUrl = card.getAttribute('data-bs-url'); // Get url of the game
    const gameTitle = card.getAttribute('data-bs-game-title'); // Title of game

    modalElement.querySelector('#modal-title').textContent = gameTitle; // Set the title of the modal to the game title
    modalElement.querySelector('#gameId').value = gameId ;// Set the gameId to a hidden input in the form so that we can use it for post
    modalElement.querySelector('#rating').value = ''; // Set the rating to previous entered rating
    modalElement.querySelector('#note').value = ''; // Set note to be what was previously set
    modalElement.querySelector('#modal-image').src = gameUrl; // Set modal image the games image url
});

document.getElementById('submitGameModal').addEventListener('click', function () {
    const gameId = document.getElementById('gameId').value;
    const rating = document.getElementById('rating').value;
    const note = document.getElementById('note').value;

    // Takes in rating and note, makes sure they meet data validation, if it fails, prevents continuing with fetch request
    if (!validateRatingNote(rating,note)) {
        return; // Exits code
    }

    const data = { gameId, rating, note }; // Create an object containing the rating and note

    // Use fetch to send a put request which sends JSON to the controller
    fetch(`/user-games`, {
        method: 'POST',
        body: JSON.stringify(data), // Pass the data into the body
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (response.ok) { // If game is added without errors, display a toast, close the modal and reload
                showToast("Game added to list", "bg-success text-white", "Success", "<i class=\"fa-solid fa-circle-check fa-xl\" style=\"color: #008000;\"></i>");
                modal.hide();

                // Find the card associated with game added by id, then add a new span which uses bootstrap badge
                const card = document.querySelector(`[data-bs-game-id="${gameId}"]`);
                const badge = document.createElement('span');
                badge.className = 'badge bg-success';
                badge.textContent = 'On Your List';

                // Make sure there's no badge before adding, to avoid duplicated badges
                if (!card.querySelector('.badge.bg-success')) {
                    card.querySelector('.card-text').appendChild(badge); // Add badge at end of card-text, where it is originally
                }
            } else if (response.status === 409) { // 409 is error for conflict, which will be thrown if game is already in the list
                showToast("This game is already in your list.", "bg-warning text-dark", "Warning", "<i class=\"fa-solid fa-circle-exclamation fa-xl\" style=\"color: #ff8000;\"></i>");
            } else { // If the request fails for any other reason, display generic error toast
                showToast("Failed to add game to list", "bg-danger text-white", "Error", "<i class=\"fa-solid fa-circle-xmark fa-xl\" style=\"color: #ff0000;\"></i>");
            }
        });
});