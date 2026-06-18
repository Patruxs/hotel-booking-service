import { useState, type ImgHTMLAttributes } from "react";

type AppImageProps = ImgHTMLAttributes<HTMLImageElement> & {
  fallbackSrc?: string;
};

export function AppImage({ src, alt, className, loading = "lazy", fallbackSrc = "/globe.svg", ...props }: AppImageProps) {
  const [currentSrc, setCurrentSrc] = useState(src || fallbackSrc);

  return (
    <img
      {...props}
      src={currentSrc}
      alt={alt}
      className={className}
      loading={loading}
      onError={() => setCurrentSrc(fallbackSrc)}
    />
  );
}
