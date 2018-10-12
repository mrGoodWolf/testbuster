package mchorse.blockbuster.commands.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.api.Model;
import mchorse.blockbuster.api.ModelPose;
import mchorse.blockbuster.client.model.ModelCustom;
import mchorse.blockbuster.client.model.parsing.ModelExporterOBJ;
import mchorse.blockbuster.common.CommonProxy;
import mchorse.blockbuster.utils.L10n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class SubCommandModelExportObj extends CommandBase
{
    @Override
    public String getName()
    {
        return "export_obj";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "blockbuster.commands.model.export_obj";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException(this.getUsage(sender));
        }

        String modelName = args[0];
        ModelCustom model = ModelCustom.MODELS.get(modelName);

        if (model == null)
        {
            throw new CommandException("blockbuster.error.model.export.no_model", modelName);
        }

        Model data = model.model;
        ModelPose pose = args.length >= 2 ? data.getPose(args[1]) : data.getPose("standing");
        String obj = new ModelExporterOBJ(data, pose).export(modelName);

        /* Save */
        String filename = modelName.replaceAll("[^\\w\\d_-]", "_");
        File destination = new File(CommonProxy.configFile, "export/" + filename + ".obj");

        if (data.defaultTexture != null)
        {
            try
            {
                String mtl = "# MTL generated by Blockbuster (version " + Blockbuster.VERSION + ")\n\nnewmtl default\nKd 1.000000 1.000000 1.000000\nmap_Kd " + filename + ".png";
                File mtlFile = new File(CommonProxy.configFile, "export/" + filename + ".mtl");
                FileUtils.writeStringToFile(mtlFile, mtl);
            }
            catch (Exception e)
            {}

            try
            {
                IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(data.defaultTexture);
                BufferedImage image = TextureUtil.readBufferedImage(resource.getInputStream());
                File texture = new File(CommonProxy.configFile, "export/" + filename + ".png");

                ImageIO.write(image, "png", texture);
            }
            catch (Exception e)
            {}
        }

        try
        {
            FileUtils.writeStringToFile(destination, obj);

            L10n.success(sender, "model.export.obj", modelName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            L10n.error(sender, "model.export.obj", modelName);
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, ModelCustom.MODELS.keySet());
        }

        return super.getTabCompletions(server, sender, args, pos);
    }
}