export function cloneTemplate(htmlId: string): Node {
    const template: HTMLTemplateElement = document.getElementById(htmlId)!! as HTMLTemplateElement
    return template.content.cloneNode(true)
}

export function copyTemplate(htmlId: string): DocumentFragment {
    const template: HTMLTemplateElement = document.getElementById(htmlId)!! as HTMLTemplateElement
    const newElem = document.importNode(template.content, true)

    return newElem
}