import { copyTemplate } from "./utils"

export interface Module {
    inject(): void
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