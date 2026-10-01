#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform BlobConfig {
    float Radius;
};

in vec2 texCoord;

out vec4 fragColor;

// Circular max filter (dilation): bright areas spread into blobs instead of smoothly blurring
void main() {
    vec2 oneTexel = 1.0 / InSize;
    vec4 maxVal = texture(InSampler, texCoord);
    for (float u = 0.0; u <= Radius; u += 1.0) {
        for (float v = 0.0; v <= Radius; v += 1.0) {
            if (sqrt(u * u + v * v) > Radius) continue;

            vec4 s0 = texture(InSampler, texCoord + vec2(-u * oneTexel.x, -v * oneTexel.y));
            vec4 s1 = texture(InSampler, texCoord + vec2( u * oneTexel.x,  v * oneTexel.y));
            vec4 s2 = texture(InSampler, texCoord + vec2(-u * oneTexel.x,  v * oneTexel.y));
            vec4 s3 = texture(InSampler, texCoord + vec2( u * oneTexel.x, -v * oneTexel.y));

            maxVal = max(maxVal, max(max(s0, s1), max(s2, s3)));
        }
    }

    fragColor = vec4(maxVal.rgb, 1.0);
}
