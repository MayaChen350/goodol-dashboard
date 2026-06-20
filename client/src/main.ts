import { Component } from './core/types'
import { initModules } from './modules/init'
import './style.css'

export class MenuElem extends Component {
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
    // potentially: checking every fields before updating
    const link = this.it!!.querySelector("a")!!

    link.href = this.url
    link.textContent = this.name
  }
}

// MAIN
initModules()
