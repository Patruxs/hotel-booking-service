import { useState, type CSSProperties, type ImgHTMLAttributes } from "react";

type AppImageProps = ImgHTMLAttributes<HTMLImageElement> & {
  fallbackSrc?: string;
  fill?: boolean;
  priority?: boolean;
  objectFit?: CSSProperties["objectFit"];
};

export function AppImage({ src, alt, className, loading, fallbackSrc = "/globe.svg", fill, priority, objectFit, style, ...props }: AppImageProps) {
  const [currentSrc, setCurrentSrc] = useState(src || fallbackSrc);

  return (
    <img
      {...props}
      src={currentSrc}
      alt={alt}
      className={className}
      loading={priority ? "eager" : (loading ?? "lazy")}
      style={{
        ...(fill ? { height: "100%", inset: 0, objectFit, position: "absolute", width: "100%" } : null),
        ...style,
      }}
      onError={() => setCurrentSrc(fallbackSrc)}
    />
  );
}
