function adminAction(endpoint) {
    const url = document.getElementById('adminUrl').value;
    const keywords = document.getElementById('adminKeywords').value;
    const formData = new FormData();
    formData.append('url', url);
    if (keywords) formData.append('keywords', keywords);
    fetch(endpoint, {
        method: 'POST',
        body: formData
    }).then(response => response.text()).then(alert);
}

function loadKeywordsForUrl() {
    const url = document.getElementById('adminUrl').value;
    fetch('admin/load-keywords?url=' + encodeURIComponent(url))
        .then(response => response.text())
        .then(keywords => {
            document.getElementById('adminKeywords').value = keywords;
        });
}