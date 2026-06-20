export function cloneTemplate(htmlId: string): Node {
    const template: HTMLTemplateElement = document.getElementById(htmlId)!! as HTMLTemplateElement
    return template.content.cloneNode(true)
}

export function copyTemplate(htmlId: string): DocumentFragment {
    const template: HTMLTemplateElement = document.getElementById(htmlId)!! as HTMLTemplateElement
    const newElem = document.importNode(template.content, true)

    if (newElem !instanceof HTMLElement) {
        throw Error("the template node was not html")
    } else return newElem
}