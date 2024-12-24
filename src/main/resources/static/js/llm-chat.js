document.getElementById('chat-form').addEventListener('submit', async (event) => {
    event.preventDefault();

    const prompt = document.getElementById('prompt').value;
    const responseText = document.getElementById('response-text');
    const csrfInput = document.querySelector('input[name="_csrf"]');
    const csrfToken = csrfInput.value;
    responseText.textContent = "Загрузка...";

    try {
        const response = await fetch('/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken,
            },
            body: JSON.stringify({
                model: 'qwen2.5:14b',
                prompt: prompt,
            }),
        });


        const data = await response.text();

        if (data) {
            responseText.textContent = data;
        } else {
            responseText.textContent = "Ответ пустой или некорректный.";
        }
    } catch (error) {
        console.error('Ошибка при запросе:', error);
        responseText.textContent = "Произошла ошибка при запросе.";
    }
});