/**
 * Takes in rating and note and makes sure rating is 1-5 and note is less than 255 characters.
 *
 * @param {string} rating - Takes in the entered rating
 * @param {string} note - Takes in the entered note
 * @returns {boolean} - Returns false if rating or note don't meet data validation
 */
function validateRatingNote(rating, note) {
    // Data validation for rating to ensure it's between 1 and 5, returns a toast if it isn't
    if (rating < 1 || rating > 5) {
        showToast("Rating must be between 1 and 5", "bg-danger text-white","Error","<i class=\"fa-solid fa-circle-xmark fa-xl\" style=\"color: #ff0000;\"></i>");
        return false;  // Exits without updating the entry or submitting form
    }

    // Data validation for note, makes sure it's less than 255 characters, otherwise displays a toast
    if (note.length > 255) {
        showToast("Note cannot be longer than 255 characters", "bg-danger text-white","Error","<i class=\"fa-solid fa-circle-xmark fa-xl\" style=\"color: #ff0000;\"></i>");
        return false;  // Exits without updating the entry or submitting form
    }
    return true;
}



