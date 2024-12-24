// document.addEventListener('DOMContentLoaded', function () {
//     const radioDivs = document.querySelectorAll('.radio-div');
//     const realRadioButtons = document.querySelectorAll('.hidden-radio');
//
//     console.log('Количество радиокнопок:', realRadioButtons.length);
//
//     radioDivs.forEach((radioDiv, index) => {
//         if (realRadioButtons[index].checked) {
//             radioDiv.classList.add('checked');
//         }
//
//         radioDiv.addEventListener('click', function () {
//             const groupName = realRadioButtons[index].name;
//
//             realRadioButtons.forEach((radio, i) => {
//                 if (radio.name === groupName) {
//                     radio.checked = false;
//                     radioDivs[i].classList.remove('checked');
//                 }
//             });
//
//             realRadioButtons[index].checked = true;
//             radioDiv.classList.add('checked');
//
//             console.log(`Радиокнопка ${index} выбрана:`, realRadioButtons[index].checked);
//         });
//     });
// });
//
//
// document.getElementById('datePicker').addEventListener('change', function () {
//     document.getElementById('dateSelection').submit();
// });
//
// const radioButtonsExec = document.querySelectorAll('input[name="executors"]');
// const radioButtonsTime = document.querySelectorAll('input[name="timeslots_radio"]');
// radioButtonsExec.forEach(radio => {
//     radio.addEventListener('change', function () {
//         document.getElementById('executorSelection').submit();
//     });
// });
// const submitButton = document.getElementById('submitBtn');
//
// radioButtonsTime.forEach(radio => {
//     radio.addEventListener('change', function () {
//         submitButton.style.display = 'block';
//     });
// });

document.addEventListener('DOMContentLoaded', function () {
    const radioDivs = document.querySelectorAll('.radio-div');
    const realRadioButtons = document.querySelectorAll('.hidden-radio');
    const executorForm = document.getElementById('executorSelection');
    const submitButton = document.getElementById('submitBtn');

    console.log('Количество радиокнопок:', realRadioButtons.length);

    // Обработка радио-кнопок для выбора исполнителя
    radioDivs.forEach((radioDiv, index) => {
        if (realRadioButtons[index].checked) {
            radioDiv.classList.add('checked');
        }

        radioDiv.addEventListener('click', function () {
            const groupName = realRadioButtons[index].name;

            // Сбрасываем все радио-кнопки в этой группе
            realRadioButtons.forEach((radio, i) => {
                if (radio.name === groupName) {
                    radio.checked = false;
                    radioDivs[i].classList.remove('checked');
                }
            });

            // Устанавливаем выбранное значение
            realRadioButtons[index].checked = true;
            radioDiv.classList.add('checked');

            // Ручной вызов события "change"
            const changeEvent = new Event('change', { bubbles: true });
            realRadioButtons[index].dispatchEvent(changeEvent);

            console.log(`Радиокнопка ${index} выбрана:`, realRadioButtons[index].checked);

            // Если это группа "executors", отправляем форму
            if (groupName === 'executors') {
                executorForm.submit();
            }
        });
    });

    // Показ кнопки отправки для выбора времени
    const radioButtonsTime = document.querySelectorAll('input[name="timeslots_radio"]');
    radioButtonsTime.forEach(radio => {
        radio.addEventListener('change', function () {
            submitButton.style.display = 'block';
        });
    });

    // Обработка выбора даты
    document.getElementById('datePicker').addEventListener('change', function () {
        document.getElementById('dateSelection').submit();
    });
});
