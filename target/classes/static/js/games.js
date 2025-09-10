// Modal for viewing a game in the game list
const modalElement = document.getElementById('gameModal'); // Find html element using the modal's id
const modal = new bootstrap.Modal(modalElement); // Create a bootstrap modal using the element

// When the card is clicked, get the data attributes from the card and then assign them to the corresponding elements in the modal
modalElement.addEventListener('show.bs.modal', function (event) {
    const card = event.relatedTarget // The card is the one that was clicked

    // Set values on modal to values that were passed in from the card
    modalElement.querySelector('#modal-title').textContent = card.getAttribute('data-bs-game-title');
    modalElement.querySelector('#gameId').value = card.getAttribute('data-bs-game-id');
    modalElement.querySelector('#modal-image').src = card.getAttribute('data-bs-url');

    // Reset the rating and note to empty
    modalElement.querySelector('#rating').value = '';
    modalElement.querySelector('#note').value = '';
});

// When clicking the add game button, Send POST request
document.getElementById('submitGameModal').addEventListener('click', function () {
    const gameId = document.getElementById('gameId').value; // ID of game
    const rating = document.getElementById('rating').value; // Rating
    const note = document.getElementById('note').value; // Note

    // Make note and rating meet validations rules
    if (validateRatingNote(rating, note)) {
        const data = {gameId, rating, note};
        fetch(`/user-games`, {
            method: 'POST',
            body: JSON.stringify(data), // Pass the data into the body
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (response.ok) { // If game is added without errors, display a toast, close the modal and add badge to card
                    showToast("Game added to list", "Success");
                    modal.hide();

                    // Find the card associated with game added by id, then add a new span which uses bootstrap badge
                    const card = document.querySelector(`[data-bs-game-id="${gameId}"]`);
                    const badge = document.createElement('span');
                    badge.className = 'badge bg-success';
                    badge.textContent = 'On Your List';

                    // Make sure there's no badge before adding, to avoid duplicated badges
                    if (!card.querySelector('.badge.bg-success')) {
                        card.querySelector('.card-text').appendChild(badge);
                    }
                } else {
                    if (response.status === 409) { // 409 is error for conflict, which will be thrown if game is already in the list
                        showToast("This game is already in your list.", "Warning");
                    } else { // If the request fails for any other reason, display error toast
                        showToast("Failed to add game to list", "Error");
                    }
                }
            });
    }
});