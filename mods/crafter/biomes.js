/*
 Crafter - A blocky game (engine) written in Java with LWJGL.
 Copyright (C) 2023  jordan4ibanez

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

// Biome registration

crafter.registerBiome(
    new BiomeDefinition("classic")
        .setScale(20.0)
        .setFrequency(0.01)
        .setGrassLayer("crafter:grass")
        .setDirtLayer("crafter:dirt")
        .setStoneLayer("crafter:stone")
        // ore definition goes here
        // cave noise definition goes here
);