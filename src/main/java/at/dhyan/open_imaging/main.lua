--[===================================================================[--
   Copyright © 2016, 2018 Pedro Gimeno Fortea. All rights reserved.

   Permission is hereby granted to everyone to copy and use this file,
   for any purpose, in whole or in part, free of charge, provided this
   single condition is met: The above copyright notice, together with
   this permission grant and the disclaimer below, should be included
   in all copies of this software or of a substantial portion of it.

   THIS SOFTWARE COMES WITHOUT ANY WARRANTY, EXPRESS OR IMPLIED.
--]===================================================================]--

-- Command-line GIF viewer

-- We've disabled these in conf.lua - re-enable them here
require 'love.window'
require 'love.graphics'

if arg[#arg]=="-debug"then local m=require"mobdebug"m.start()m.coro()table.remove(arg)end

local gifnew = require 'gifload.gifload'
local twimer = require 'twimer.twimer'()

local gif, nframe, nloops, canvas, checker, chkquad

local function DoFrame()
  local prevframe = nframe
  nframe = nframe + 1
  if nframe > gif.nimages then
    if not gif.loop then
      return
    end

    if gif.loop ~= 0 then
      nloops = nloops + 1
      if nloops >= gif.loop then
        return
      end
    end
    
    nframe = 1
  end
  local dispose = prevframe == 0 and 0 or gif.imgs[prevframe*5-4]
  local delay = gif.imgs[nframe*5-3]
  if gif.imgs[nframe*5-4] ~= 3 then -- Paint if CURRENT dispose is not 'undo'
    love.graphics.setCanvas(canvas)
    -- Clear if PREVIOUS dispose is not 'combine'
    --[[if dispose == 0 then
      love.graphics.clear()
    end]]
    if dispose == 2 then
      local r, g, b = 0, 0, 0
      if type(gif.background) == "table" then
        r, g, b = unpack(gif.background)
      end
      love.graphics.setBlendMode("replace")
      love.graphics.setColor(r, g, b, 0)
      --print("fill", gif.imgs[prevframe*5-1], gif.imgs[prevframe*5], gif.imgs[prevframe*5-2]:getDimensions())
      love.graphics.rectangle("fill", gif.imgs[prevframe*5-1], gif.imgs[prevframe*5], gif.imgs[prevframe*5-2]:getDimensions())
      love.graphics.setColor(255, 255, 255)
      love.graphics.setBlendMode("alpha")
    end
    love.graphics.draw(gif.imgs[nframe*5-2], gif.imgs[nframe*5-1], gif.imgs[nframe*5])
    love.graphics.setCanvas()
  end

  return twimer:after(delay, DoFrame)
end

local function usage()
  print("Usage: love vgif.love [--flatbg] [--vsync] [--autosize] [--progressive] image.gif")
end

function getopts(args)
  -- Parse arguments
  local expect_nonopt = false
  local nonoptions, options = {}, {}
  -- Start in args[1] if fused, otherwise in args[2] (to skip .love or dir arg)
  for i = ((love._version_major ~= 0 or love.filesystem.isFused()) and 1 or 2), #args do
    local arg = args[i]
    if expect_nonopt or arg:sub(1, 1) ~= '-' then
      nonoptions[#nonoptions + 1] = arg
    elseif arg == '--' then
      expect_nonopt = true
    else
      options[#options+1] = arg
    end
  end
  return options, nonoptions
end

function love.load(args)
  love.graphics.setDefaultFilter("nearest", "nearest")

  local options, nonoptions = getopts(args)

  if #nonoptions ~= 1 then
    usage()
    love.event.quit()
    return
  end

  local opt = {
    flatbg = false;
    vsync = false;
    autosize = false;
    progressive = false;
  }

  for _, v in ipairs(options) do
    if v == '--help' then
      usage()
      love.event.quit()
      return
    elseif v == '--flatbg' then
      opt.flatbg = true
    elseif v == '--vsync' then
      opt.vsync = true
    elseif v == '--autosize' then
      opt.autosize = true
    elseif v == "--progressive" then
      opt.progressive = true
    else
      print('Invalid option: ', v)
      usage()
      love.event.quit()
      return
    end
  end
      
  -- Paint a Loading... sign while loading the gif
  love.window.setMode(120, 20, {vsync = false, resizable = true})
  love.graphics.clear()
  love.graphics.print("Loading...")
  love.graphics.present()

  local f = io.open(nonoptions[1], 'rb')
  if not f then
    error("Can't open file: " .. nonoptions[1])
  end
  gif = gifnew()
  gif.progressive = opt.progressive
  repeat
    local s = f:read(524288)
    collectgarbage("collect") -- free the string from the previous iteration
    if not s or s == '' then
      break
    end
    gif:update(s)
  until false
  f:close()
  gif:done()

  if gif.nimages == 0 then
    print("No images in GIF file: " .. nonoptions[1])
    love.event.quit()
    return
  end

  local fullscreen = false
  local _, _, flags = love.window.getMode()
  local w, h = love.window.getDesktopDimensions(flags.display)
  local gifwidth, gifheight = 1, 1
  if opt.autosize then
    for i = 1, gif.nimages do
      gifwidth = math.max(gifwidth, gif.imgs[i*5-2]:getWidth() + gif.imgs[i*5-1])
      gifheight = math.max(gifheight, gif.imgs[i*5-2]:getHeight() + gif.imgs[i*5])
    end
  else
    gifwidth, gifheight = gif.width, gif.height
  end

  love.window.setTitle(nonoptions[1])
  -- Leave margins
  if gifwidth < w - 20 and gifheight < h - 60 or gifwidth == w and gifheight == h then
    w, h = gifwidth, gifheight
  else
    fullscreen = true
  end
  love.window.setMode(w, h, {fullscreen = fullscreen, vsync = opt.vsync, resizable = flags.resizable})
  canvas = love.graphics.newCanvas()
  if not opt.flatbg then
    checker = love.graphics.newImage(gifnew():update("GIF87a\16\0\16\0\240\1\0fff\153\153\153,\0\0\0\0\16\0\16\0\0\2\31\140o\160\171\136\204\220\129K&\10l\192\217r\253y\28\6\146\34U\162'\148\178k\244VV\1\0;"):done().imgs[3])
    checker:setWrap("repeat", "repeat")
    chkquad = love.graphics.newQuad(0, 0, w, h, 16, 16)
  end

  -- Convert ImageData to Image
  for i = 1, gif.nimages do
    gif.imgs[i*5-2] = love.graphics.newImage(gif.imgs[i*5-2])
  end
  
  if gif.imgs[gif.nimages*5-3] == 0 then
    -- Tweak last delay to avoid stack overflow on images that have 0 delay in every frame
    gif.imgs[gif.nimages*5-3] = 1e-60
  end

  nloops = 0
  nframe = 0

  if type(gif.background) == "table" then
    love.graphics.setBackgroundColor(gif.background)
  end

  return twimer:after(1e-60, DoFrame) -- can't be 0 because it would be triggered out of love.draw
end

function love.update(dt)
  twimer:update(dt)
end

function love.draw()
  if checker then
    love.graphics.draw(checker, chkquad)
  end

  love.graphics.draw(canvas)
  if nframe <= gif.nimages then
    if gif.imgs[nframe*5-4] == 3 then -- Restore After Drawing disposal mode?
      -- If so, it hasn't been drawn because that would pollute the canvas. Draw it now.
      love.graphics.draw(gif.imgs[nframe*5-2], gif.imgs[nframe*5-1], gif.imgs[nframe*5])
    end
  end
end

function love.resize(w, h)
  if checker then
    chkquad = love.graphics.newQuad(0, 0, w, h, 16, 16)
  end
end

function love.keypressed(k)
  if k == "escape" then love.event.quit() end
end
