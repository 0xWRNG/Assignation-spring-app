document.addEventListener('DOMContentLoaded', function () {
    const checkboxDivs = document.querySelectorAll('.checkbox-div');
    const realCheckboxes = document.querySelectorAll('.hidden-checkbox');

    console.log('Количество чекбоксов:', realCheckboxes.length);

    checkboxDivs.forEach((checkboxDiv, index) => {
        if (realCheckboxes[index].checked) {
            checkboxDiv.classList.add('checked');
        }

        checkboxDiv.addEventListener('click', function () {
            realCheckboxes[index].checked = !realCheckboxes[index].checked;
            checkboxDiv.classList.toggle('checked', realCheckboxes[index].checked);
            console.log(realCheckboxes[index].checked)
        });
    });
});
