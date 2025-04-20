// Modal for editing a game in the users game list
const modalElement = document.getElementById('gameModal'); // Find html element using the modal's id
const modal = new bootstrap.Modal(modalElement); // Create a bootstrap modal using the element

// When the card is clicked, get the data attributes from the card and then assign them to the corresponding elements in the modal
modalElement.addEventListener('show.bs.modal', function (event) {
    const card = event.relatedTarget // The card is the one that was clicked
    const gameId = card.getAttribute('data-bs-game-id'); // ID of the game
    const gameUrl = card.getAttribute('data-bs-game-url'); // Cover image url
    const gameTitle = card.getAttribute('data-bs-game-title'); // Title
    const rating = card.getAttribute('data-bs-game-rating'); // Rating
    const note = card.getAttribute('data-bs-game-note'); // Note

    modalElement.querySelector('#gameId').value = gameId; // Set the hidden input field's value to the game's id
    modalElement.querySelector('#modal-title').textContent = gameTitle; // Set the title of the modal to the game title
    modalElement.querySelector('#rating').value = rating; // Set the rating to previously entered rating
    modalElement.querySelector('#note').value = note; // Set note to be what was previously set
    modalElement.querySelector('#modal-image').src = gameUrl; // Set image at the top of modal to the game's cover
});

// When clicking the remove game button, Send DELETE request using game id
document.getElementById('removeGame').addEventListener('click', function () {
    const gameId = document.getElementById('gameId').value; // Get the game id
    fetch(`/user-games/${gameId}`, {method: 'DELETE'}) // Send DELETE request with the game id
        .then(response => {
            if (response.ok) { // If game is deleted from list, show toast
                showToast("Game deleted", "bg-success text-white", "Success", "<i class=\"fa-solid fa-circle-check fa-xl\" style=\"color: #008000;\"></i>");
                modal.hide(); // Close the modal
                // Find card with the associated game id and select the parent container
                const card = document.querySelector(`[data-bs-game-id="${gameId}"]`).closest('.col-md-4.mb-4');
                if (card) { // If the card was found
                    card.remove(); // Remove it
                }
            } else { // If DELETE failed, show a toast
                showToast("Failed to delete game", "bg-danger text-white", "Error", "<i class=\"fa-solid fa-circle-xmark fa-xl\" style=\"color: #ff0000;\"></i>");
            }
        });
});

// When the submit update button is pressed, send PUT request
document.getElementById('submitGameModal').addEventListener('click', function () {
    const gameId = document.getElementById('gameId').value; // Game id
    const rating = document.getElementById('rating').value; // Rating
    const note = document.getElementById('note').value; // Note

    // Pass in rating and note, returns false if either doesn't meet validation rules (1-5 rating, 255 char max for note)
    if (!validateRatingNote(rating, note)) {
        return; // Exits without updating
    }

    const data = {rating, note}; // Create an object containing the rating and note

    // Use fetch to send PUT request which sends JSON to the controller
    fetch(`/user-games/${gameId}`, {
        method: 'PUT',
        body: JSON.stringify(data), // Pass the data into the body
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (response.ok) { // If game is updated show toast
                showToast("Game updated successfully", "bg-success text-white", "Success", "<i class=\"fa-solid fa-circle-check fa-xl\" style=\"color: #008000;\"></i>");
                modal.hide(); // Close the modal
                const card = document.querySelector(`[data-bs-game-id="${gameId}"]`); // Card with the ID of game updated
                if (card) { // If the card exists
                    const stars = card.querySelector('.stars'); // Get rating element
                    const noteElement = card.querySelector('.note'); // Get note element

                    // Set HTML of stars span, loops creating star icons 1-5 times based on rating
                    stars.innerHTML = '';
                    for (let i = 0; i < rating; i++) {
                        const star = document.createElement('i');
                        star.classList.add('fa-solid', 'fa-star', 'text-warning');
                        stars.appendChild(star);
                    }

                    noteElement.textContent = note; // Update note displayed on card

                    // Update modal data-bs-* attributes so re-opening card passes in new values
                    card.setAttribute('data-bs-game-rating', rating);
                    card.setAttribute('data-bs-game-note', note);
                }
            } else { // If there was an error updating, display a toast
                showToast("Failed to update game", "bg-danger text-white", "Error", "<i class=\"fa-solid fa-circle-xmark fa-xl\" style=\"color: #ff0000;\"></i>");
            }
        });
});
