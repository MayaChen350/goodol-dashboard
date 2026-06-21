import { Component, type Module } from "../types.ts";


export class MenuItem extends Component {
    protected readonly templateId: string = "link-template"

    public name: string
    public url: string
    public color: string

    public it: null | ParentNode = null

    constructor(name: string, url: string, color: string) {
        super()

        this.name = name
        this.url = url
        this.color = color
    }

    update() {
        // potentially: checking every field before updating
        const link = this.it!!.querySelector("a")!!

        link.href = this.url
        link.textContent = this.name
    }
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
        const menuElem = new MenuItem(this.name, this.url, this.color)
        menuElem.init(document.body.querySelector("#app")!!)
    }
}