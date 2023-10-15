const internalEntity = Java.type("engine.entity").INSTANCE

const entity = {
  registerGeneric: function(definition) {
    internalEntity.registerGeneric((internalDef) => {
      for (const key of Object.keys(definition)) {
        internalDef[key] = definition[key]
      }
    })
  },
  spawn: function(name) {
    internalEntity.spawn(name)
  }
}

entity.registerGeneric({
  name: "crafter:debug",
  blah: function() {
    println("hello, I AM A CRAFTER ENTITY!")
    println(this.self.x)
  },
  onStep: function(dtime) {
    println(`dtime is: ${dtime + 0.1}`)
    // println(dtime)
  }
  // onstep: 1
})

entity.spawn("crafter:debug")
