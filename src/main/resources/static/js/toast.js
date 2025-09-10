/**
 * Basic toast setup using bootstrap. Displays toast based on values passed in
 *
 * @param {string} message The message to display in the body of the toast
 * @param title The title of the toast, typically; Success, Error or Warning
 */
function showToast(message, title) {
    // Styling for toast based on title (success/error/warning)
    let type = 'bg-success text-white';
    let icon = '<i class=\"fa-solid fa-circle-check fa-xl\" style=\"color: #008000;\"></i>';

    switch (title){
        case 'Error':
            type = 'bg-danger text-white';
            icon = '<i class=\"fa-solid fa-circle-xmark fa-xl\" style=\"color: #ff0000;\"></i>';
            break;
        case 'Warning':
            type = 'bg-warning text-dark';
            icon = '<i class=\"fa-solid fa-circle-exclamation fa-xl\" style=\"color: #ff8000;\"></i>';
            break;
    }

    const toastHTML = `
        <div class="toast ${type}" role="alert" aria-live="assertive" aria-atomic="true" data-bs-delay="3000">
            <div class="toast-header">
                <strong class="me-auto ">${icon} ${title}</strong>
                <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
            <div class="toast-body">
                ${message}
            </div>
        </div>
    `;

    // Find the toast container and append new toast's HTML to end
    const toastContainer = document.getElementById('toastContainer');
    toastContainer.insertAdjacentHTML('beforeend', toastHTML);

    // Select the last toast (one we just added) and initialize/show it
    const toastElement = toastContainer.lastElementChild;
    new bootstrap.Toast(toastElement).show();
}
