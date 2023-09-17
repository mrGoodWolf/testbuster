# gifload - love2d GIF&#8480; image loader.

This is a LÖVE (love2d) module that implements reading of GIF images, implemented in pure LuaJIT + FFI.

## Usage

The only file that is needed is gifload/gifload.lua. Put it into any directory of your choice where you can readily `require()` it. The rest of files in the repository are used for a demo, a GIF animation viewer designed to be invoked from the command-line, explained later.

This module requires the `love.image` module to be loaded. It's active by default, unless manually disabled in `love.conf` or the module is used in a thread. In the latter case, just use `require 'love.image'` in the thread to load it.

Requiring the file returns a function that will create GIF objects. Invoke it for example like this:

```lua
local gifNew = require('gifload.gifload')
```

Then you can call `gifNew()` to create a GIF object; you will need to create one every time you want to load a new GIF file:

```lua
local gif = gifNew()
```

Each GIF object has three methods: `update`, `done` and `frame`. Call `gif:update(data)` every time you have new data to feed to it, and `gif:done()` when finished sending data. The `update` method takes one single parameter of type *string*. This enables processing large files without needing to load the content into memory, or downloading GIF files from the internet showing the partial image as the download progresses. Both the `update` and `done` method return the GIF object, so that you can concatenate method calls when possible if desired. The `frame` method returns five parameters corresponding to a frame once loaded, given the frame number (1-based). It's documented below.

Before calling the `update` method, there are a few fields that may be changed in the object, to alter its behaviour:

- Setting `gif.progressive = true` will cause interlaced images to be fully filled after each interlacing pass, as opposed to just one scanline. This is useful when loading images progressively, to quickly preview the contents as the image loads. Filling the blanks wastes a few cycles, so if it's not needed (for example if images are always fully loaded before displaying starts), it's best to leave it disabled, which is the default.
- `gif.err` is a callback function that is called whenever an error happens. The callback receives the GIF object and the error message as parameters. The default `gif.err` function just invokes `print` with the error message. If the function returns, it will resume operation when possible, or stop otherwise. Note that the callback is invoked from within a coroutine, which is important if you want to call `error()`. The coroutine is wrapped in such a way that any errors in the coroutine are propagated to the main thread, though the reported line will not match the actual line where the error occurs.

```lua
local version = gifNew("version")
```

Available since version 1.0.2 only, returns the version of the library, in hexadecimal. The format is 0xMMmmpp, where MM is the major version number, mm is the minor, and pp is the patch, all of which are two-digit hexadecimal numbers. For example, version 1.0.2 is represented as 0x010002.

### Example: loading a GIF file

```lua
local gifNew = require('gifload.gifload')

local function loadGif(path)
  local gifFile = love.filesystem.newFile(path, 'r')
  if not gifFile then
    return nil
  end
  local gif = gifNew()

  repeat
    local s = gifFile:read(65536)
    if s == nil or s == "" then
      break
    end

    gif:update(s)
  until false

  gifFile:close()

  return gif:done()
end

local myGif = loadGif('assets/images/myGif.gif')
```

The image data is loaded into a Lua table with the following format:

```lua
  {
    -- Methods
    update = function;
    done = function;
    frame = function;
    -- Settings
    err = ErrorFunction;
    progressive = BOOL;
    -- Loaded data
    background = {R, G, B} or N;
    width = N;
    height = N;
    imgs = {
      disposal1, delay1, imageData1, positionX1, positionY1,
      disposal2, delay2, imageData2, positionX2, positionY2,
      ... };
    nimages = N;
    ncomplete = N;
    loop = BOOL;
    aspect = N;
  }
```

Several fields may contain `false` or 0 when not enough data has been read yet (e.g. right after creation of the GIF object).

`err` and `progressive` are set by the programmer, not by the GIF file, as explained before.

`background` may contain:

- `false` if the GIF header hasn't been read yet
- a table with this structure: `{R, G, B}` (values between 0 and 255), if the background colour has an entry in the palette;
- a number if the background colour index was bigger than the palette size, or if the palette has not been loaded yet.

`width` and `height` contain the GIF image's global width and height as specified in the GIF header, or `false` if the header hasn't been read yet.

`imgs` is a sequence containing 5 elements per image in the GIF file. To ease obtaining these five elements, there is a method called `frame` that returns all 5 given a frame number (where 1 is the first frame), like this:

```
local imgData, positionX, positionY, delay, disposal = gif:frame(N)
```

The images themselves (first element returned by `frame`) are in the form of [**ImageData**](https://love2d.org/wiki/ImageData) LÖVE objects. These can be passed through [love.graphics.newImage](https://love2d.org/wiki/love.graphics.newImage) to obtain an [**Image**](https://love2d.org/wiki/Image) object that can be drawn.

`positionX` and `positionY` are the relative position where the current image should be drawn with respect to the GIF's top left corner.

`delay` (in seconds) is supposed to be applied after the frame has been displayed.

`disposal` can have eight values:

- 0 means all previous contents should be left as-is, and the current image should be superimposed on the previous frame.
- 1 means the current frame is the result of superimposing the current image on the previous frame (which practically produces the same result as 0).
- 2 means the same as 1, but the current image's bounding box should be restored to the background colour prior to displaying the next frame.
- 3 means the same as 1, but the previous frame should be restored before displaying the next frame, as if the current image wasn't drawn.
- Values 4 to 7 are undefined in the GIF specification and should never appear in a GIF89a file. If one such value appears, the GIF is invalid.

For a detailed explanation of frame disposal methods, you can read http://www.imagemagick.org/Usage/anim_basics/#dispose - 1 corresponds to *Dispose None*, 3 to *Dispose Previous* and 2 to *Dispose Background*.

`nimages` is the number of images currently loaded (partially or fully, doesn't matter). It should equal `#imgs/5`.

`ncomplete` is the number of complete images loaded so far. It is one less than `nimages` while an image is loading, or equal to `nimages` if all the images found so far are fully loaded.

`loop` is either `false` if the animation shouldn't loop, or a number indicating how many times it should loop, with 0 meaning infinite. Note that a value of 1 is equivalent to `false`.

`aspect` is the pixel aspect ratio (pixel width divided by pixel height) specified in the GIF file. 1.0 means the pixels are square; a value greater than 1.0 means the pixels are wider than they are tall, and a value less than 1.0 means the pixels are taller than they are wide. Today most devices' pixels are square, so this parameter can be used to scale the image in such a way that the original aspect ratio is restored.

## Demo program: GIF image/animation viewer

To provide an example of using the library, a demo program is included. The program is designed to be invoked from a console/terminal. Invoke it like this:

```
love /path/to/vgif.love [--help] [--flatbg] [--vsync] [--autosize] [--progressive] /path/to/image.gif
```

The option `--flatbg` means that instead of using a grey checkerboard pattern, transparent areas should be displayed by filling them with the background colour.

The option `--vsync` is used to activate vsync. Since the program has a limit that the minimum GIF frame rate is the minimum LÖVE frame rate, this imposes further limitations to the minimum GIF frame rate.

The option `--autosize` means to ignore the width and height specified in the GIF header, and calculate them from the frames instead. Some broken GIF files may need this option to be displayed properly.

The option `--progressive` activates the lib's progressive mode, but since frames are only displayed after they are fully loaded, it won't have any visible effect unless the file is truncated. Of course, it has no effect whatsoever unless the gif is interlaced.

The program ignores the `aspect` field, rendering every source pixel to a corresponding destination pixel, with no aspect ratio correction.

The `escape` key exits the program while displaying.

Download the .love file from: https://notabug.org/attachments/da46227e-4be6-4b13-8999-7a975f8332c2

Works with LÖVE versions 0.9 and 0.10.

## Problems?

If you find a GIF file that this program loads incorrectly, or that the example program renders incorrectly, please file an issue in the project's [issue tracker](https://notabug.org/pgimeno/gifload/issues).

## License

gifload is free software. It is published under the following license, which can be seen as a simplified version of the Expat license:

```
   Copyright © 2016, 2018 Pedro Gimeno Fortea. All rights reserved.

   Permission is hereby granted to everyone to copy and use this file,
   for any purpose, in whole or in part, free of charge, provided this
   single condition is met: The above copyright notice, together with
   this permission grant and the disclaimer below, should be included
   in all copies of this software or of a substantial portion of it.

   THIS SOFTWARE COMES WITHOUT ANY WARRANTY, EXPRESS OR IMPLIED.
```

The example program itself is free software too, but it needs a library called *twimer* which is not yet released at the time of writing, and is therefore only provided to make the example work, but not intended for use in your own projects yet.
