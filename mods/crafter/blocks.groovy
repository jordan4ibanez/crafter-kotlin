package crafter

import engine.DrawType
import engine.block

final block = block.INSTANCE

block.register("test", "test", new String[]{"test"}, DrawType.BLOCK)