/**
 * Takes in rating and note and makes sure rating is between 1-5 and note is at max 255 characters.
 *
 * @param {string} rating - Takes in the entered rating
 * @param {string} note - Takes in the entered note
 * @returns {boolean} - Returns false if rating or note don't meet data validation
 */
function validateRatingNote(rating, note) {
    // Rating must be between 1 and 5
    if (rating < 1 || rating > 5) {
        showToast("Rating must be between 1 and 5", "Error");
        return false;
    }

    // Note must be a maximum of 255 characters
    if (note.length > 255) {
        showToast("Note cannot be longer than 255 characters", "Error");
        return false;
    }

    return true;
}



