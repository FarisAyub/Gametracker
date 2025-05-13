// Modal for viewing a game in the game list
const modalElement = document.getElementById('gameModal'); // Find html element using the modal's id
const modal = new bootstrap.Modal(modalElement); // Create a bootstrap modal using the element

// When the card is clicked, get the data attributes from the card and then assign them to the corresponding elements in the modal
modalElement.addEventListener('show.bs.modal', function (event) {
    const card = event.relatedTarget // The card is the one that was clicked
    const gameId = card.getAttribute('data-bs-game-id'); // Game's id
    const gameUrl = card.getAttribute('data-bs-url'); // Cover image url
    const gameTitle = card.getAttribute('data-bs-game-title'); // Title

    modalElement.querySelector('#modal-title').textContent = gameTitle; // Set title of modal to the game title
    modalElement.querySelector('#gameId').value = gameId; // Set the hidden input field's value to the game's id
    modalElement.querySelector('#rating').value = ''; // Set the rating to previously entered rating
    modalElement.querySelector('#note').value = ''; // Set note to be what was previously set
    modalElement.querySelector('#modal-image').src = gameUrl; // Set image at the top of modal to the game's cover
});

// When clicking the add game button, Send POST request
document.getElementById('submitGameModal').addEventListener('click', function () {
    const gameId = document.getElementById('gameId').value; // ID of game
    const rating = document.getElementById('rating').value; // Rating
    const note = document.getElementById('note').value; // Note
    const csrfToken = document.getElementById('csrfToken').value; // Get csrf for authentication

    if (!validateRatingNote(rating, note)) { // Pass in rating and note, returns false if either doesn't meet validation rules (1-5 rating, 255 char max for note)
        return; // Exits without adding game
    }

    const data = {gameId, rating, note}; // Create an object containing the rating and note

    // Use fetch to send POST request which sends JSON to the controller
    fetch(`/user-games`, {
        method: 'POST',
        body: JSON.stringify(data), // Pass the data into the body
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrfToken // Include CSRF token in the header
        }
    })
        .then(response => {
            if (response.ok) { // If game is added without errors, display a toast, close the modal and add badge to card
                showToast("Game added to list", "bg-success text-white", "Success", "<i class=\"fa-solid fa-circle-check fa-xl\" style=\"color: #008000;\"></i>");
                modal.hide();

                // Find the card associated with game added by id, then add a new span which uses bootstrap badge
                const card = document.querySelector(`[data-bs-game-id="${gameId}"]`);
                const badge = document.createElement('span');
                badge.className = 'badge bg-success';
                badge.textContent = 'On Your List';

                // Make sure there's no badge before adding, to avoid duplicated badges
                if (!card.querySelector('.badge.bg-success')) {
                    card.querySelector('.card-text').appendChild(badge); // Add badge at end of card-text, where it is normally shows
                }
            } else if (response.status === 409) { // 409 is error for conflict, which will be thrown if game is already in the list
                showToast("This game is already in your list.", "bg-warning text-dark", "Warning", "<i class=\"fa-solid fa-circle-exclamation fa-xl\" style=\"color: #ff8000;\"></i>");
            } else { // If the request fails for any other reason, display error toast
                showToast("Failed to add game to list", "bg-danger text-white", "Error", "<i class=\"fa-solid fa-circle-xmark fa-xl\" style=\"color: #ff0000;\"></i>");
            }
        });
});