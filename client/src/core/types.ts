import { MenuElem } from "../main"
import { copyTemplate } from "./utils"

export interface Module {
    inject(): void
}

export class MenuMod implements Module {
    public name: string
    public url: string
    public color: string

    constructor(name: string, url: string, color: string) {
        this.name = name
        this.url = url
        this.color = color
    }

    inject(): void {
        const menuElem = new MenuElem(this.name, this.url, this.color)
        menuElem.init(document.body.querySelector("#app")!!)
    }
}

export abstract class Component {
    protected abstract readonly templateId: string

    public it: null | ParentNode = null

    initAt(querySelector: string) {
        this.init(document.querySelector(querySelector)!!)
    }

    init(where: ParentNode) {
        if (this.it !== null) throw Error("Element of template: #" + this.templateId + " was attempted to be initialized twice!")

        const root = this.it = copyTemplate(this.templateId)
        this.update()

        const subReference = root.firstChild!!;
        where.appendChild(root)

        // assign a reference in the dom to the element
        this.it = subReference.parentNode
    }

    abstract update(): void
}