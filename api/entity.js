const internalEntity = Java.type("engine.entity").INSTANCE

entity.registerGeneric((definition) => {
  print(definition.x)
})
