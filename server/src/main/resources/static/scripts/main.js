document.getElementById("btn-click-me").addEventListener("click", async () => {
    const data = await fetch("/html-dsl");
    document.querySelector("body").outerHTML += await data.text();
});
