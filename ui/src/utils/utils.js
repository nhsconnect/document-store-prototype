import dayjs from "dayjs";

export const setUrlHostToLocalHost = (url) => {
    const isDevelopmentEnv = !process.env.NODE_ENV || process.env.NODE_ENV === "development";

    if (isDevelopmentEnv) {
        const urlObject = new URL(url);
        urlObject.host = "localhost";

        return urlObject.toString();
    } else {
        return url;
    }
};

export const formatSize = (bytes) => {
    if (bytes === 0) {
        return "0 bytes";
    }

    const units = ["bytes", "KB", "MB", "GB"];
    const bytesPerKilobyte = 1024;
    const exponent = Math.floor(Math.log(bytes) / Math.log(bytesPerKilobyte));
    const divisor = Math.pow(bytesPerKilobyte, exponent);

    if (!units[exponent]) {
        throw new Error("Invalid file size");
    }

    return `${Math.round(bytes / divisor)} ${units[exponent]}`;
};

export const toFileList = (files) => {
    const updatedFileList = new DataTransfer();

    files.forEach((file) => {
        updatedFileList.items.add(file);
    });

    return updatedFileList.files;
};

export const downloadFile = (url, filename) => {
    const downloadLink = document.createElement("a");
    downloadLink.href = url;
    downloadLink.download = filename;

    downloadLink.click();
};

export const getFormattedDate = (date) => {
    if (!date || (typeof date === "string" && date.length < 8)) {
        return "Invalid date";
    }

    return dayjs(date).format("D MMMM YYYY");
};
