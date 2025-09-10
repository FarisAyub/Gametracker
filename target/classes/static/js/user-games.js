// Modal for editing a game in the users game list
const modalElement = document.getElementById('gameModal'); // Find html element using the modal's id
const modal = new bootstrap.Modal(modalElement); // Create bootstrap moda

// When the card is clicked, get the data attributes from the card and then assign them to the corresponding elements in the modal
modalElement.addEventListener('show.bs.modal', function (event) {
    const card = event.relatedTarget;

    // Set the modal details to previously entered values
    modalElement.querySelector('#modal-image').src = card.getAttribute('data-bs-game-url');
    modalElement.querySelector('#modal-title').textContent = card.getAttribute('data-bs-game-title');
    modalElement.querySelector('#gameId').value = card.getAttribute('data-bs-game-id'); // Hidden input

    modalElement.querySelector('#rating').value = card.getAttribute('data-bs-game-rating');
    modalElement.querySelector('#note').value = card.getAttribute('data-bs-game-note');
});

// When clicking the remove game button, Send DELETE request using game id
document.getElementById('removeGame').addEventListener('click', function () {
    const gameId = document.getElementById('gameId').value; // Get the game id

    fetch(`/user-games/${gameId}`, {method: 'DELETE'})
        .then(response => {
            if (response.ok) { // If game is deleted from list, show toast
                showToast("Game deleted", "Success");
                modal.hide(); // Close the modal
                // Find card with the associated game id and select the parent container
                const card = document.querySelector(`[data-bs-game-id="${gameId}"]`).closest('.col-md-4.mb-4');
                if (card) { // If the card was found
                    card.remove(); // Remove it
                }
            } else { // If DELETE failed, show a toast
                showToast("Failed to delete game",  "Error");
            }
        });
});

// When the submit update button is pressed, send PUT request
document.getElementById('submitGameModal').addEventListener('click', function () {
    const gameId = document.getElementById('gameId').value; // Game id
    const rating = document.getElementById('rating').value; // Rating
    const note = document.getElementById('note').value; // Note

    // Check the new rating and note meet the validation rules
    if (validateRatingNote(rating, note)) {
        const data = {rating, note};
        fetch(`/user-games/${gameId}`, {
            method: 'PUT',
            body: JSON.stringify(data), // Pass the data into the body
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (response.ok) { // If game is updated show toast
                    showToast("Game updated successfully", "Success");
                    modal.hide(); // Close the modal
                    const card = document.querySelector(`[data-bs-game-id="${gameId}"]`); // Find card for current game
                    if (card) { // If the card exists
                        const stars = card.querySelector('.stars'); // Get rating element
                        const noteElement = card.querySelector('.note'); // Get note element

                        // Set HTML of stars span, loops creating star icons 1-5 times based on rating
                        stars.innerHTML = '';
                        for (let i = 0; i < 5; i++) {
                            const star = document.createElement('i');
                            // Append empty stars for stars after rating, e.g. 3/5 will have 2 empty stars at end
                            if (i + 1 <= rating) {
                                star.classList.add('fa-solid', 'fa-star', 'text-warning');
                            } else {
                                star.classList.add('fa-regular', 'fa-star', 'text-warning');
                            }
                            stars.appendChild(star);
                        }

                        noteElement.textContent = note; // Replace note on the card with the updated one

                        // Update modal data-bs-* attributes so re-opening card to edit again will display new values
                        card.setAttribute('data-bs-game-rating', rating);
                        card.setAttribute('data-bs-game-note', note);
                    }
                } else { // If failed to update
                    showToast("Failed to update game", "Error");
                }
            });
    }
});
