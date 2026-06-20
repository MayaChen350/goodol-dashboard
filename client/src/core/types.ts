import { MenuElem } from "../main"

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