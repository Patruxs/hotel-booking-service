import { AppImage as Image } from "@/components/AppImage";
import React from "react";
function FeatureSection() {
  return (
    <section className="py-20 px-4 bg-background">
      <div className="container mx-auto">
        <div className="text-center mb-16">
          <h2 className="text-4xl md:text-5xl font-bold mb-4 text-foreground">
            LUXURY{" "}
            <span className="inline-flex items-center gap-2">
              <span className="w-8 h-0.5 bg-accent"></span>
            </span>{" "}
            APARTMENTS
          </h2>
          <p className="text-xl text-muted-foreground">
            PREMIUM APARTMENT SERVICES AT TIMES SQUARE
          </p>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-12">
          <div className="relative aspect-square rounded-full overflow-hidden">
            <Image
                src="https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699218/small_vojtech-bruzek-Yrxr3bsPdS0-unsplash_ciknsd.jpg"
              alt="Phòng ngủ sang trọng"
              className="w-full h-full object-cover"
              width={500}
              height={500}
            />
          </div>
          <div className="relative aspect-square rounded-full overflow-hidden">
            <Image
                src="https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699217/small_the-anam-_twiIcIsp2s-unsplash_wlcji8.jpg"
              alt="Phòng tắm lát đá cẩm thạch"
              className="w-full h-full object-cover"
              width={500}
              height={500}
            />
          </div>
          <div className="relative aspect-square rounded-full overflow-hidden">
            <Image
                src="https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699218/small_valeriia-bugaiova-_pPHgeHz1uk-unsplash_i0vbzz.jpg"
              alt="Phòng khách sang trọng"
              className="w-full h-full object-cover"
              width={500}
              height={500}
            />
          </div>
        </div>
        <div className="max-w-3xl mx-auto text-center">
          <p className="text-muted-foreground leading-relaxed text-lg">
            Discover the perfect blend of comfort and sophistication in our
            meticulously designed apartments. Each space is furnished with
            premium interiors, modern amenities, and offers breathtaking
            panoramic views of the city.
          </p>
        </div>
      </div>
    </section>
  );
}
export default FeatureSection;
