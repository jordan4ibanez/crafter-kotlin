const internalEntity = Java.type("engine.entity").INSTANCE

const entity = {
  registerGeneric: function(definition) {
    internalEntity.registerGeneric((internalDef) => {
      for (const key of Object.keys(definition)) {
        internalDef[key] = definition[key]
      }
    })
  }
}

entity.registerGeneric({
  name: "crafter:debug",
  blah: function() {
    println("hello, I AM A CRAFTER ENTITY!")
    println(this.self.x)
  },
})
