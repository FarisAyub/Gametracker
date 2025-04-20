/**
 * Takes in rating and note and makes sure rating is 1-5 and note is less than 255 characters.
 *
 * @param {string} rating - Takes in the entered rating
 * @param {string} note - Takes in the entered note
 * @returns {boolean} - Returns false if rating or note don't meet data validation
 */
function validateRatingNote(rating, note) {
    if (rating < 1 || rating > 5) {
        showToast("Rating must be between 1 and 5", "bg-danger text-white", "Error", "<i class=\"fa-solid fa-circle-xmark fa-xl\" style=\"color: #ff0000;\"></i>");
        return false;  // Returns false immediately, regardless of note was correct
    }

    if (note.length > 255) {
        showToast("Note cannot be longer than 255 characters", "bg-danger text-white", "Error", "<i class=\"fa-solid fa-circle-xmark fa-xl\" style=\"color: #ff0000;\"></i>");
        return false;  // Returns false regardless of if rating was correct
    }
    return true;
}



