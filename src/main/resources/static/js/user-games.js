// Modal for editing a game in the users game list
const modalElement = document.getElementById('gameModal'); // Find the modal by id
const modal = new bootstrap.Modal(modalElement); // Create modal

// On clicking on the card, get the data attributes from the card and then assign them to the fields in the modal
modalElement.addEventListener('show.bs.modal', function (event) {
    const cardModal = event.relatedTarget // Find the card that triggered the modal
    const gameId = cardModal.getAttribute('data-bs-game-id'); // Get the games ID from the button and assign it to a variable
    const gameUrl = cardModal.getAttribute('data-bs-game-url'); // Get url of the game
    const gameTitle = cardModal.getAttribute('data-bs-game-title'); // Title of game
    const rating = cardModal.getAttribute('data-bs-game-rating'); // Title of game
    const note = cardModal.getAttribute('data-bs-game-note'); // Title of game

    modalElement.querySelector('#gameId').value = gameId ;// Set the gameId to a hidden input in the form so that we can use it for post
    modalElement.querySelector('#modal-title').textContent = gameTitle; // Set the title of the modal to the game title
    modalElement.querySelector('#rating').value = rating; // Set the rating to previous entered rating
    modalElement.querySelector('#note').value = note; // Set note to be what was previously set
    modalElement.querySelector('#modal-image').src = gameUrl; // Set modal image the games image url
});

// On clicking the remove game button, find the game id from the hidden input and send a DELETE request
// to remove the game from the user-games list
document.getElementById('removeGame').addEventListener('click', function () {
    const gameId = document.getElementById('gameId').value; // Get the hidden game id input field
    fetch(`/user-games/${gameId}`, { method: 'DELETE' }) // Send delete operation
        .then(response => {
            if (response.ok) { // If game is deleted from list, create a toast and reload
                showToast("Game deleted", "bg-success text-white","Success","<i class=\"fa-solid fa-circle-check fa-xl\" style=\"color: #008000;\"></i>");
                modal.hide();
                // Remove the card from the DOM without reloading the page
                const card = document.querySelector(`[data-bs-game-id="${gameId}"]`).closest('.col-md-4.mb-4');
                if (card) {
                    card.remove(); // Remove the parent container that holds the card
                }
            } else { // If failed to delete, show a toast to let user know
                showToast("Failed to delete game", "bg-danger text-white","Error","<i class=\"fa-solid fa-circle-xmark fa-xl\" style=\"color: #ff0000;\"></i>");
            }
        });
});

// When the submit update button is pressed, send update request
document.getElementById('submitGameModal').addEventListener('click', function () {
    const gameId = document.getElementById('gameId').value; // Get game id from form
    const rating = document.getElementById('rating').value; // Get rating from form
    const note = document.getElementById('note').value; // Get note from form

    // Takes in rating and note, makes sure they meet data validation, if it fails, prevents continuing with fetch request
    if (!validateRatingNote(rating,note)) {
        return; // Exits code
    }

    console.log('Rating:', rating);
    console.log('Note:', note);
    const data = { rating, note }; // Create an object containing the rating and note

    // Use fetch to send a put request which sends JSON to the controller
    fetch(`/user-games/${gameId}`, {
        method: 'PUT',
        body: JSON.stringify(data), // Pass the data into the body
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (response.ok) { // If game is updated without errors, display a toast, close the modal and reload
                showToast("Game updated successfully", "bg-success text-white","Success","<i class=\"fa-solid fa-circle-check fa-xl\" style=\"color: #008000;\"></i>");
                modal.hide();
                // Now update the specific card in the list
                const card = document.querySelector(`[data-bs-game-id="${gameId}"]`);
                if (card) {
                    // Update the card's rating (stars) and note based on the new values
                    const stars = card.querySelector('.stars');
                    const noteElement = card.querySelector('.note');

                    // Update stars based on new rating
                    stars.innerHTML = '';
                    for (let i = 0; i < rating; i++) {
                        const star = document.createElement('i');
                        star.classList.add('fa-solid', 'fa-star', 'text-warning');
                        stars.appendChild(star);
                    }

                    // Update note text
                    noteElement.textContent = note;


                    // Update modal data-bs-* attributes as well so that when it's reopened, the new values are passed
                    card.setAttribute('data-bs-game-rating', rating);
                    card.setAttribute('data-bs-game-note', note);
                }
            } else { // If there was an error, display a toast
                showToast("Failed to update game", "bg-danger text-white","Error","<i class=\"fa-solid fa-circle-xmark fa-xl\" style=\"color: #ff0000;\"></i>");
            }
        });
});
