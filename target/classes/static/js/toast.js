/**
 * Basic toast setup using bootstrap. Displays toast based on values passed in
 *
 * @param {string} message The message to display in the body of the toast
 * @param {string} type The class colour to use for the toast modifying colour between yellow, red and green
 * @param title The title of the toast, typically; Success, Error or Warning
 * @param icon A HTMl span containing an icon from Font Awesome with a preset colour
 */
function showToast(message, type, title, icon) {
    document.getElementById('toastContainer').innerHTML = `
        <div class="toast ${type}" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="3000">
            <div class="toast-header">
                <strong class="me-auto ">${icon} ${title}</strong>
                <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
            <div class="toast-body">
                ${message}
            </div>
        </div>
    `; // Replace the html in the toast container to current html
    const toastElement = document.querySelector('.toast'); // Select the toast element
    const toast = new bootstrap.Toast(toastElement); // Create the toast
    toast.show() // Show the toast, applying the animations/fade-in etc
}