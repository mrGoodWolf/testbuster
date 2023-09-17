--[===================================================================[--
   Copyright © 2016 Pedro Gimeno Fortea. All rights reserved.

   Permission is hereby granted to everyone to copy and use this file,
   for any purpose, in whole or in part, free of charge, provided this
   single condition is met: The above copyright notice, together with
   this permission grant and the disclaimer below, should be included
   in all copies of this software or of a substantial portion of it.

   THIS SOFTWARE COMES WITHOUT ANY WARRANTY, EXPRESS OR IMPLIED.
--]===================================================================]--

-- LÖVE configuration file

function love.conf(c)
  -- Module overrides - there's a lot we don't need
  c.modules.audio = false
  c.modules.joystick = false
  c.modules.math = false
  c.modules.mouse = false
  c.modules.physics = false
  c.modules.sound = false
  c.modules.system = false
  c.modules.touch = false
  c.modules.video = false
  c.modules.thread = false
  
  -- Disable window and graphics, so the window isn't created
  -- until we're about to load the gif (e.g. --help won't
  -- show a window)
  c.modules.window = false
  c.modules.graphics = false
end
