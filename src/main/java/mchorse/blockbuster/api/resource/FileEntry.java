package mchorse.blockbuster.api.resource;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileEntry implements IResourceEntry
{
    public File file;
    public long lastModified;

    public FileEntry(File file)
    {
        this.file = file;
        this.lastModified = this.lastModified();
    }

    @Override
    public String getName()
    {
        return this.file == null ? "" : this.file.getName();
    }

    @Override
    public InputStream getStream() throws IOException
    {
        return this.file == null ? null : new FileInputStream(this.file);
    }

    @Override
    public boolean exists()
    {
        return this.file != null && this.file.exists();
    }

    @Override
    public boolean hasChanged()
    {
        long lastModified = this.lastModified();
        boolean result = lastModified > this.lastModified;

        this.lastModified = lastModified;

        return result;
    }

    @Override
    public long lastModified()
    {
        return this.file == null ? 0 : this.file.lastModified();
    }

    @Override
    public boolean copyTo(File file)
    {
        try
        {
            FileUtils.copyFile(this.file, file);

            return true;
        }
        catch (IOException e)
        {}

        return false;
    }
}
