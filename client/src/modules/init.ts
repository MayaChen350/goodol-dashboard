import type { Module } from "../core/types";
import { weeklyTaskingMod } from "./weeklyTasking/init";

// Inject all modules at the root 
export function initModules() {
    const modules: Module[] = [
        weeklyTaskingMod
    ]

    modules.forEach(mod => mod.inject());
}