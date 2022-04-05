Array.from(document.getElementsByClassName("markdown")).forEach(x => x.innerHTML = marked.parse(x.innerHTML))
