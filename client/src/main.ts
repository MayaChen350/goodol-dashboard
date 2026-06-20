import { copyTemplate } from './core/utils'
import { initModules } from './modules/init'
import './style.css'

export class MenuElem {
  private readonly templateId: string = "link-template"

  public name: string
  public url: string
  public color: string

  public it: null | ParentNode = null

  constructor(name: string, url: string, color: string) {
    this.name = name
    this.url = url
    this.color = color
  }

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

  update() {
    // potentially: checking every fields before updating
    const link = this.it!!.querySelector("a")!!

    link.href = this.url
    link.textContent = this.name
  }
}

// MAIN
initModules()
