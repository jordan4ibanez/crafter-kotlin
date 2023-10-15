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
  onLoad: function() {
    println("hello, I AM A CRAFTER ENTITY!")
  },
  onStep: function(dtime) {
    println(dtime)
    this.set("hi", [1, 2, 3])
    this.get("hi").forEach(element => {
      println(element)
    });
  },
})

entity.spawn("crafter:debug")
