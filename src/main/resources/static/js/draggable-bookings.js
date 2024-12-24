function drag(ev) {
    ev.dataTransfer.setData("text", ev.target.getAttribute("data-id"));
}

function allowDrop(ev) {
    ev.preventDefault();
    if (ev.target.classList.contains('card-wrapper') || ev.target.closest('.card-wrapper')) {
        ev.dataTransfer.dropEffect = "none";
    } else {
        ev.dataTransfer.dropEffect = "move";
    }
}


document.querySelectorAll('.col-4').forEach(col => {
    col.addEventListener('dragover', allowDrop);
    col.addEventListener('drop', drop);
});

function drop(ev) {
    ev.preventDefault();
    var data = ev.dataTransfer.getData("text");
    var card = document.querySelector(`.card-wrapper[data-id='${data}']`);

    var newStatus = ev.target.closest('.col-4').querySelector('h1').id;

    var oldColumn = card.parentNode;

    ev.target.appendChild(card);
    updateCardColor(card, newStatus);
    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    fetch(`/update-status/${data}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-CSRF-TOKEN': csrfToken,
        },
        body: `status=${encodeURIComponent(newStatus)}`
    })
        .then(response => {
            if (!response.ok) {
                console.error('Ошибка при изменении статуса');
                oldColumn.appendChild(card);
                updateCardColor(card, card.getAttribute('data-prev-status'));
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            oldColumn.appendChild(card);
            updateCardColor(card, card.getAttribute('data-prev-status'));
        });
}

function updateCardColor(card, status) {
    card.classList.remove('approved', 'not_approved', 'canceled'); // Убираем все старые классы

    if (status === 'approved') {
        card.classList.add('approved');
    } else if (status === 'not_approved') {
        card.classList.add('not_approved');
    } else {
        card.classList.add('canceled');
    }
}