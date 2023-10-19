const internalEntity = Java.type("engine.entity").INSTANCE

const entity = {
  register: function(definition) {
    internalEntity.register((internalDef) => {
      for (const key of Object.keys(definition)) {
        internalDef[key] = definition[key]
      }
    })
  },
  spawn: function(name) {
    internalEntity.spawn(name)
  }
}

entity.register({
  name: "crafter:debug",
  onSpawn: function() {

  },
  onLoad: function() {
    println("hello, I AM A CRAFTER ENTITY!")
  },
  onStep: function(dtime) {
    println(dtime)
    this.set("hi", [1, 2, 3])
  },
})

entity.spawn("crafter:debug")
